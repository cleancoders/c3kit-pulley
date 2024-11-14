(ns c3kit.bucket.pulley-rest
  (:require [c3kit.apron.legend :as legend]
            [c3kit.apron.utilc :as utilc]
            [c3kit.bucket.api :as api]
            [c3kit.pulley.core :as pcore]
            [c3kit.pulley.http :as http]))

(defn proxy-error [url response]
  (ex-info (str "Pulley REST request failed. URL: " url " " (:status response) " " (:body response)) response))

(defn coerce [legend entity]
  (->> entity
       pcore/keyword-kind
       (legend/coerce! @legend)))

(defn body->entity [legend response]
  (->> (:body response)
       pcore/body->json
       (coerce legend)))

(defn clear [url-base]
  (let [response (http/delete! url-base)]
    (when-not (= 204 (:status response))
      (throw (proxy-error url-base response)))))

(defn entity [legend url-base kind id]
  (let [id       (if (map? id) (:id map) id)
        url      (str url-base "/" (name kind) "/" (str id))
        response (http/get! url)]
    (case (:status response)
      200 (body->entity legend response)
      404 nil
      (throw (proxy-error url response)))))

(defn tx [legend url-base entity]
  (let [url      (str url-base "/" (name (:kind entity)))
        response (http/put! url (utilc/->json entity))]
    (case (:status response)
      200 (body->entity legend response)
      (throw (proxy-error url response)))))

(defn tx* [legend url-base entities]
  (let [response (http/put! url-base (utilc/->json entities))]
    (case (:status response)
      200 (->> (:body response)
               pcore/body->json
               (map #(coerce legend %)))
      (throw (proxy-error url-base response)))))

(deftype PulleyRestDB [legend url-base]
  api/DB
  (-clear [this] (clear url-base))
  (close [_this] (println "close"))
  (-count [this kind options] (println "-count"))
  (-delete-all [this kind] (println "-delete-all"))
  (-entity [this kind id] (entity legend url-base kind id))
  (-find [this kind options] (println "-find"))
  (-reduce [this kind f init options] (println "-reduce"))
  (-tx [this entity] (tx legend url-base entity))
  (-tx* [this entities] (tx* legend url-base entities)))

(defmethod api/-create-impl :pulley-rest [config schemas]
  (let [{:keys [url-base]} config]
    (PulleyRestDB. (atom (legend/build schemas)) url-base)))