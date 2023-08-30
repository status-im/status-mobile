(ns quo2.components.wallet.account-origin.component-spec
  (:require [test-helpers.component :as h]
            [quo2.core :as quo]
            [utils.i18n :as i18n]))

(h/describe
 "account origin tests"
 (h/test "component renders"
         (h/render [quo/account-origin
                    {:type :recovery-phrase
                     :stored :on-keycard
                     :derivation-path "m / 44’ / 60’ / 0’ / 0’ / 2"
                     :user-name "Alisher Yakupov"
                     :on-press    (h/mock-fn)}])
         (h/is-truthy (h/get-by-text (i18n/label "origin"))))
 
 (h/test "recovery phrase icon is visible when :type is :recovery-phrase"
         (h/render [quo/account-origin
                    {:type :recovery-phrase
                     :stored :on-keycard
                     :derivation-path "m / 44’ / 60’ / 0’ / 0’ / 2"
                     :user-name "Alisher Yakupov"
                     :on-press    (h/mock-fn)}])
         (h/is-truthy (h/get-by-label-text :recovery-phrase-icon)))
 
 (h/test "recovery phrase icon is visible when :type is :recovery-phrase"
         (h/render [quo/account-origin
                    {:type :recovery-phrase
                     :stored :on-keycard
                     :derivation-path "m / 44’ / 60’ / 0’ / 0’ / 2"
                     :user-name "Alisher Yakupov"
                     :on-press    (h/mock-fn)}])
         (h/is-truthy (h/get-by-label-text :recovery-phrase-icon)))
 
 (h/test "private-key-icon is visible when :type is :private-key"
         (h/render [quo/account-origin
                    {:type :private-key
                     :stored :on-keycard
                     :derivation-path "m / 44’ / 60’ / 0’ / 0’ / 2"
                     :user-name "Alisher Yakupov"
                     :on-press    (h/mock-fn)}])
         (h/is-truthy (h/get-by-label-text :private-key-icon)))
 
 (h/test "derivation-path-icon is visible when :type is :default-keypair"
         (h/render [quo/account-origin
                    {:type :default-keypair
                     :stored :on-keycard
                     :derivation-path "m / 44’ / 60’ / 0’ / 0’ / 2"
                     :user-name "Alisher Yakupov"
                     :on-press    (h/mock-fn)}])
         (h/is-truthy (h/get-by-label-text :derivation-path-icon)))
 
 (h/test "derivation-path-icon is visible when :type is :recovery-phrase"
         (h/render [quo/account-origin
                    {:type :recovery-phrase
                     :stored :on-keycard
                     :derivation-path "m / 44’ / 60’ / 0’ / 0’ / 2"
                     :user-name "Alisher Yakupov"
                     :on-press    (h/mock-fn)}])
         (h/is-truthy (h/get-by-label-text :derivation-path-icon))) 
 
  (h/test "on-device text is visible when :stored is :on-device"
         (h/render [quo/account-origin
                    {:type :recovery-phrase
                     :stored :on-device
                     :derivation-path "m / 44’ / 60’ / 0’ / 0’ / 2"
                     :user-name "Alisher Yakupov"
                     :on-press    (h/mock-fn)}])
         (h/is-truthy (h/get-by-text (i18n/label "on-device"))))
 
  (h/test "on-keycard text is visible when :stored is :on-keycard"
         (h/render [quo/account-origin
                    {:type :recovery-phrase
                     :stored :on-keycard
                     :derivation-path "m / 44’ / 60’ / 0’ / 0’ / 2"
                     :user-name "Alisher Yakupov"
                     :on-press    (h/mock-fn)}])
         (h/is-truthy (h/get-by-text (i18n/label "on-keycard"))))
  
   (h/test "on-press is called when :type is :recovery-phrase"
           (let [on-press (h/mock-fn)]
          (h/render [quo/account-origin
                     {:type :recovery-phrase
                      :stored :on-keycard
                      :derivation-path "m / 44’ / 60’ / 0’ / 0’ / 2"
                      :user-name "Alisher Yakupov"
                      :on-press    on-press}])
             (h/fire-event :press (h/query-by-label-text :derivation-path-button))
          (h/was-called on-press)))
  
  (h/test "on-press is called when :type is :default-keypair"
          (let [on-press (h/mock-fn)]
            (h/render [quo/account-origin
                       {:type :default-keypair
                        :stored :on-keycard
                        :derivation-path "m / 44’ / 60’ / 0’ / 0’ / 2"
                        :user-name "Alisher Yakupov"
                        :on-press    on-press}])
            (h/fire-event :press (h/query-by-label-text :derivation-path-button))
            (h/was-called on-press))))
