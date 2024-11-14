(ns c3kit.pulley.core
  (:require [c3kit.apron.utilc :as util]
            [clojure.data.json :as json]
            [clojure.java.io :as io]))


(defn body->json [body]
  (try
    (cond (nil? body) {}
          (string? body) (util/<-json-kw body)
          :else (with-open [in body]
                  (json/read (io/reader in) :key-fn keyword)))
    (catch Exception e e)))

(defn keyword-kind [entity]
  (let [kind (:kind entity)]
    (cond (nil? kind) entity
          (string? kind) (update entity :kind keyword)
          :else entity)))