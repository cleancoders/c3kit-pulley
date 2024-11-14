(ns c3kit.pulley.rest-with-server-spec
  (:require [c3kit.bucket.api :as api]
            [c3kit.bucket.impl-spec :as spec]
            [c3kit.bucket.memory :as memory]
            [c3kit.pulley.handlers :as handlers]
            [c3kit.bucket.pulley-rest]
            [org.httpkit.server :as httpkit-server]
            [speclj.core :refer :all])
  (:import (clojure.lang IDeref)))

(def config {:impl     :pulley-rest
             :url-base "http://127.0.0.1:8888"})

(deftype Derefn [f]
  IDeref
  (deref [_]
    (f)))

(def shutdown-handle (atom nil))
(def memory-db (memory/->MemoryDB (->Derefn #(deref (.-legend @api/impl))) (atom {})))
(defn start-server []
  (let [handler   (handlers/root-handler-for memory-db)]
    (reset! shutdown-handle (httpkit-server/run-server handler {:port 8888}))))
(defn stop-server []
  (when-let [handle @shutdown-handle]
    (handle)))

(describe "REST Impl"

  (before-all (api/set-safety! false)
              (api/clear- memory-db)
              (start-server))
  (after-all (stop-server))

  (spec/crud-specs config)
  ;(spec/nil-value-specs config)
  ;(spec/count-specs config)
  ;(spec/find-specs config)
  ;(spec/filter-specs config)
  ;(spec/reduce-specs config)
  ;(spec/kind-in-entity-is-optional config)
  ;(spec/broken-in-datomic config)
  ;(spec/multi-value-fields config)
  ;(spec/cas config)
  )