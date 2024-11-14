(ns c3kit.pulley.http-spec
  (:require [speclj.core #?(:clj :refer :cljs :refer-macros) [after after-all around around-all before before before-all 
                                                              context describe focus-context focus-describe focus-it it 
                                                              pending should should-be should-be-a should-be-nil 
                                                              should-be-same should-contain should-end-with should-fail 
                                                              should-have-invoked should-invoke should-not should-not 
                                                              should-not-be should-not-be-a should-not-be-nil 
                                                              should-not-be-same should-not-contain should-not-end-with 
                                                              should-not-have-invoked should-not-invoke 
                                                              should-not-start-with should-not-throw should-not=
                                                              should-not== should-start-with should-throw should< 
                                                              should<= should= should== should> should>= stub tags 
                                                              with with-all with-stubs xit redefs-around]]
            [c3kit.pulley.http :as sut]
            #?(:clj  [org.httpkit.client :as client]
               ;:cljs [cljs-http.client :as client]
               )
            [speclj.stub :as stub]))

(describe "HTTP"
  (with-stubs)

  (redefs-around [client/request (stub :client/request {:return (delay :response)})])

  (it "request"
    (should= :response @(sut/request {})))

  (it "request!"
    (should= :response (sut/request! {})))

  (context "post!"
    (it "executes synchronously"
      (should= :response (sut/post! "/example" {:foo :bar}))
      (should-have-invoked :client/request {:with [{:method :post :url "/example" :body {:foo :bar}}]}))

    (it "accepts additional request options"
      (should= :response (sut/post! "/blah" {:x :y} :headers {"Content-Type" "application/json"} :baz "buzz"))
      (should= [{:method :post :url "/blah" :body {:x :y} :headers {"Content-Type" "application/json"} :baz "buzz"}]
               (stub/last-invocation-of :client/request)))

    (it "default attributes override options"
      (should= :response (sut/post! "/foo" {:x :y} :method :head :url "/blah" :body "meh"))
      (should= [{:method :post :url "/foo" :body {:x :y}}]
               (stub/last-invocation-of :client/request)))

    (context "json params-type"
      (it "missing body"
        (sut/post! "/foo" nil :params-type :json)
        (should= [{:method  :post
                   :url     "/foo"
                   :body    "null"
                   :headers {"Content-Type" "application/json"}}]
                 (stub/last-invocation-of :client/request)))

      (it "empty map"
        (sut/post! "/foo" {} :params-type :json)
        (should= [{:method  :post
                   :url     "/foo"
                   :body    "{}"
                   :headers {"Content-Type" "application/json"}}]
                 (stub/last-invocation-of :client/request)))

      (it "populated map"
        (sut/post! "/foo" {:foo {:bar :baz}} :params-type :json)
        (should= [{:method  :post
                   :url     "/foo"
                   :body    "{\"foo\":{\"bar\":\"baz\"}}"
                   :headers {"Content-Type" "application/json"}}]
                 (stub/last-invocation-of :client/request)))

      (it "raw text is serialized as JSON"
        (sut/post! "/foo" "blah" :params-type :json)
        (should= [{:method  :post
                   :url     "/foo"
                   :body    "\"blah\""
                   :headers {"Content-Type" "application/json"}}]
                 (stub/last-invocation-of :client/request)))

      (it "Content-Type is overwritten"
        (sut/post! "/foo" {} :headers {"Content-Type" "text/html"} :params-type :json)
        (should= [{:method  :post
                   :url     "/foo"
                   :body    "{}"
                   :headers {"Content-Type" "application/json"}}]
                 (stub/last-invocation-of :client/request)))

      )

    (context "form params-type"

      (it "renames :body to :form-params"
        (sut/post! "/foo" {"bar" "baz"} :params-type :form)
        (should= [{:method      :post
                   :url         "/foo"
                   :form-params {"bar" "baz"}}]
                 (stub/last-invocation-of :client/request))
        )

      )

    (context "option builders"

      (it "with-json-params"
        (should= {:params-type :json} (sut/with-json-params))
        (should= {:params-type :json :foo "bar"} (sut/with-json-params {:foo "bar"})))

      (it "with-form-params"
        (should= {:params-type :form} (sut/with-form-params))
        (should= {:params-type :form :foo "bar"} (sut/with-form-params {:foo "bar"})))

      (it "with-auth"
        (should= {:headers {"authorization" (str "Bearer test-token")}} (sut/with-auth "test-token"))
        (should= {:headers {"authorization" (str "Bearer test-token") "foo" "bar"}}
                 (sut/with-auth {:headers {"foo" "bar"}} "test-token")))

      (it "with-basic-auth"
        (should= {:basic-auth "user:pass"} (sut/with-basic-auth "user" "pass"))
        (should= {:headers {"foo" "bar"} :basic-auth "the-username:password"}
                 (sut/with-basic-auth {:headers {"foo" "bar"}} "the-username" "password")))
      )
    )

  (context "get!"
    (it "executes synchronously"
      (should= :response (sut/get! "/example"))
      (should-have-invoked :client/request {:with [{:method :get :url "/example"}]}))

    (it "accepts additional request options"
      (should= :response (sut/get! "/blah" :headers {"Content-Type" "application/json"} :baz "buzz"))
      (should= [{:method :get :url "/blah" :headers {"Content-Type" "application/json"} :baz "buzz"}]
               (stub/last-invocation-of :client/request)))

    (it "default attributes override options"
      (should= :response (sut/get! "/foo" :method :head :url "/blah"))
      (should-have-invoked :client/request {:with [{:method :get :url "/foo"}]}))
    )

  (context "delete!"
    (it "executes synchronously"
      (should= :response (sut/delete! "/example"))
      (should-have-invoked :client/request {:with [{:method :delete :url "/example"}]}))
    )

  (context "put!"
    (it "executes synchronously"
      (should= :response (sut/put! "/example" "body"))
      (should-have-invoked :client/request {:with [{:method :put :url "/example" :body "body"}]}))
    )

  (context "put!"
    (it "executes synchronously"
      (should= :response (sut/patch! "/example" "body"))
      (should-have-invoked :client/request {:with [{:method :patch :url "/example" :body "body"}]}))
    )

  #_(context "sanitize-response"

    (it "removes authorization headers"
      (let [auth-header {"authorization" "Bearer jwt-token"}
            foo-auth    (assoc auth-header "foo" "bar")]
        (should= {} (sut/sanitize-response {}))
        (should= {} (sut/sanitize-response {:headers auth-header :opt {:headers auth-header}}))
        (should= {} (sut/sanitize-response {:headers auth-header :opts {:headers auth-header}}))
        (should= {} (sut/sanitize-response {:headers auth-header}))
        (should= {} (sut/sanitize-response {:opt {:headers auth-header}}))
        (should= {} (sut/sanitize-response {:opts {:headers auth-header}}))
        (should= {:status 200} (sut/sanitize-response {:status 200 :headers auth-header}))
        (should= {:headers {"foo" "bar"}} (sut/sanitize-response {:headers foo-auth}))
        (should= {:opt {:headers {"foo" "bar"}}} (sut/sanitize-response {:opt {:headers foo-auth}}))
        (should= {:opts {:headers {"foo" "bar"}}} (sut/sanitize-response {:opts {:headers foo-auth}}))))

    (it "removes basic auth"
      (let [basic-auth {:basic-auth "blah"}
            foo-auth   (assoc basic-auth "foo" "bar")]
        (should= {} (sut/sanitize-response {}))
        (should= {} (sut/sanitize-response (assoc basic-auth :opt basic-auth)))
        (should= {} (sut/sanitize-response (assoc basic-auth :opts basic-auth)))
        (should= {} (sut/sanitize-response basic-auth))
        (should= {} (sut/sanitize-response {:opt basic-auth}))
        (should= {} (sut/sanitize-response {:opts basic-auth}))
        (should= {:status 200} (sut/sanitize-response (assoc basic-auth :status 200)))
        (should= {"foo" "bar"} (sut/sanitize-response foo-auth))
        (should= {:opt {"foo" "bar"}} (sut/sanitize-response {:opt foo-auth}))
        (should= {:opts {"foo" "bar"}} (sut/sanitize-response {:opts foo-auth}))))
    )

  )