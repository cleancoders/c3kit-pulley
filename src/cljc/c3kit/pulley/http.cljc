(ns c3kit.pulley.http
  (:require [c3kit.apron.utilc :as utilc]
            [clojure.set :as set]
            #?(:clj  [org.httpkit.client :as client]
               ;:cljs [cljs-http.client :as client]
               )))


(defn- conform-params [{:keys [params-type] :as options}]
  (cond-> (dissoc options :params-type)
          (= :json params-type)
          (-> (assoc-in [:headers "Content-Type"] "application/json")
              (update :body utilc/->json))
          (= :form params-type)
          (set/rename-keys {:body :form-params})))

(defn- conform-options [options]
  (cond-> options (:params-type options) conform-params))

(defn request
  "Issue an HTTP request and return a promise.
   options:
     :params-type - (optional) If :json, body will be converted to json and header Content-Type updated."
  [options]
  (client/request (conform-options options)))

(defn request!
  "Issue an HTTP request synchronously."
  [options]
  (let [response @(request options)]
    response))

(defn delete!
  "Issue a DELETE request synchronously."
  [url & {:as options}]
  (request! (assoc options :method :delete :url url)))

(defn get!
  "Issue a GET request synchronously."
  [url & {:as options}]
  (request! (assoc options :method :get :url url)))

(defn patch!
  "Issue a PATCH request synchronously."
  [url body & {:as options}]
  (request! (assoc options :method :patch :url url :body body)))

(defn post!
  "Issue a POST request synchronously."
  [url body & {:as options}]
  (request! (assoc options :method :post :url url :body body)))

(defn put!
  "Issue a PUT request synchronously."
  [url body & {:as options}]
  (request! (assoc options :method :put :url url :body body)))

(defn with-params-type [options params-type]
  (assoc options :params-type params-type))

(defn with-json-params
  ([] (with-json-params {}))
  ([options] (with-params-type options :json)))

(defn with-form-params
  ([] (with-form-params {}))
  ([options] (with-params-type options :form)))

(defn with-auth
  ([token] (with-auth {} token))
  ([options token] (assoc-in options [:headers "authorization"] (str "Bearer " token))))

(defn with-basic-auth
  ([username password] (with-basic-auth {} username password))
  ([options username password] (assoc options :basic-auth (str username ":" password))))

;(def sanitized-key-paths
;  (for [root [[] [:opt] [:opts]]
;        path [[:headers "authorization"] [:basic-auth]]]
;    (concat root path)))
;
;(defn sanitize-response [response]
;  (reduce medley/dissoc-in response sanitized-key-paths))
