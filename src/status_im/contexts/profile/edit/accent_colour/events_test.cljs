(ns status-im.contexts.profile.edit.accent-colour.events-test
  (:require [cljs.test :refer [deftest is]]
            matcher-combinators.test
            [status-im.contexts.profile.edit.accent-colour.events :as sut]))

(deftest edit-accent-color-test
  (let [new-color :yellow
        key-uid   "key-uid"
        cofx      {:db {:profile/profile {:key-uid key-uid}}}
        expected  {:fx [[:json-rpc/call
                         [{:method     "wakuext_setCustomizationColor"
                           :params     [{:customizationColor new-color
                                         :keyUid             key-uid}]
                           :on-success [:profile/edit-accent-colour-success
                                        {:customization-color new-color
                                         :navigate-back?      true
                                         :show-toast?         true}]
                           :on-error   fn?}]]]}]
    (is (match? expected
                (sut/edit-accent-colour cofx [{:color new-color}])))))
