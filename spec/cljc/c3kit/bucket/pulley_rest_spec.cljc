(ns c3kit.bucket.pulley-rest-spec
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
            [c3kit.bucket.pulley-rest :as sut]
            [c3kit.bucket.api :as api]
            [c3kit.bucket.impl-spec :as spec]))

(def config {:impl :pulley-rest})

(describe "REST Impl"

  (context "creation"

    (it "defaults"
      (let [impl (api/create-db config [])]
        (should-not-be-nil impl)
        )
      )

    )
  )