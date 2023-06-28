(ns status-im2.subs.onboarding-test
  (:require [cljs.test :as t]
            [quo2.theme :as theme]
            [re-frame.db :as rf-db]
            status-im2.subs.onboarding
            [test-helpers.unit :as h]
            [utils.image-server :as image-server]
            [utils.re-frame :as rf]))

(def key-uid "0x1")
(def port "mediaserver-port")
(def cur-theme :current-theme)

(h/deftest-sub :profile/login-profiles-picture
  [sub-name]
  (with-redefs [image-server/get-account-image-uri identity
                theme/get-theme                    (constantly cur-theme)]
    (t/testing "nil when no images"
      (swap! rf-db/app-db assoc :profile/profiles-overview {key-uid {}})
      (t/is (nil? (rf/sub [sub-name key-uid]))))

    (t/testing "nil when no key-uid"
      (swap! rf-db/app-db assoc :profile/profiles-overview {key-uid {}})
      (t/is (nil? (rf/sub [sub-name "0x2"]))))

    (t/testing "result from image-server/get-account-image-uri"
      (swap!
        rf-db/app-db
        assoc
        :profile/profiles-overview {key-uid {:images [{:type "large"}
                                                      {:type "thumbnail"}]}}
        :mediaserver/port          port)
      (t/is (= (rf/sub [sub-name key-uid])
               {:port       port
                :image-name "large"
                :key-uid    key-uid
                :theme      cur-theme
                :ring?      true})))))
