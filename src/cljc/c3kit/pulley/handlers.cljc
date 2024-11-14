(ns c3kit.pulley.handlers
  (:require [c3kit.apron.schema :as schema]
            [c3kit.apron.utilc :as util]
            [c3kit.bucket.api :as api]
            [c3kit.pulley.core :as pcore]
            [compojure.core :refer [DELETE GET PUT routes]]
            [ring.util.response :as response]))

(defn maybe-set-kind [entity request]
  (if-let [kind-str (get-in request [:params :kind])]
    (assoc entity :kind (schema/->keyword kind-str))
    entity))

(defn maybe-set-id [entity request]
  (if-let [id-str (get-in request [:params :id])]
    (assoc entity :id id-str)
    entity))

(defn entity-response [result]
  (-> (response/response (util/->json result))
      (response/status 200)))

(defn clear [db]
  (api/clear- db)
  (response/status 204))

(defn entity [db request]
  (let [kind (keyword (get-in request [:params :kind]))
        id   (get-in request [:params :id])]
    (entity-response (api/entity- db kind id))))

(defn tx [db request]
  (let [entity (-> (pcore/body->json (:body request))
                   (maybe-set-id request)
                   (maybe-set-kind request))
        result (api/tx- db entity)]
    (entity-response result)))

(defn tx* [db request]
  (let [entities (pcore/body->json (:body request))
        entities (map pcore/keyword-kind entities)
        result   (api/tx* db entities)]
    (entity-response result)))

(defn root-handler-for [db]
  (routes
    (DELETE "/" [] (clear db))
    (GET "/:kind/:id" request (entity db request))
    (PUT "/" request (tx* db request))
    (PUT "/:kind" request (tx db request))
    (PUT "/:kind/:id" request (tx db request))))