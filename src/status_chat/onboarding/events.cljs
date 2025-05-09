(ns status-chat.onboarding.events
  (:require [native-module.core :as native-module]
            [re-frame.core :as rf]
            [status-chat.onboarding.constants :as chat-constants]
            [status-im.constants :as constants]
            [status-im.contexts.profile.config :as profile.config]
            status-im.contexts.profile.create.effects
            [taoensso.timbre :as log]
            [utils.security.core :as security]))

(rf/reg-event-fx :onboarding/start
 (fn [_]
   {:fx [[:dispatch [:onboarding/init-flow]]
         [:dispatch [:update-theme-and-init-root :screen/onboarding.chat]]]}))

(rf/reg-event-fx :onboarding/init-flow
 (fn [{:keys [db]}]
   (let [flow chat-constants/create-new-flow]
     {:db (assoc db
                 :onboarding/flow            flow
                 :onboarding/step-idx        0
                 :onboarding/replies-by-step {})})))

(rf/reg-event-fx :onboarding/next-step
 (fn [{:keys [db]}]
   ;; TODO: if last step, then finish the flow
   {:db (update db :onboarding/step-idx inc)}))

(rf/reg-event-fx :onboarding/store-reply
 (fn [{:keys [db]} [step value]]
   {:db (assoc-in db [:onboarding/replies-by-step step] value)}))

(rf/reg-event-fx :onboarding/submit-reply
 (fn [{:keys [db]} [step value]]
   (let [current-step-idx         (get db :onboarding/step-idx)
         submit-for-current-step? (-> db
                                      :onboarding/flow
                                      (nth current-step-idx)
                                      (= step))]
     {:fx [[:dispatch [:onboarding/store-reply step value]]
           (when submit-for-current-step?
             [:dispatch [:onboarding/next-step]])]})))

(rf/reg-event-fx :onboarding/setup-biometrics
 (fn [{:keys [db]} [{:keys [key-uid]}]]
   (let [password-hash (-> db
                           :onboarding/replies-by-step
                           :create-password
                           :input-value
                           native-module/sha3)
         enable?       (-> db
                           :onboarding/replies-by-step
                           :biometrics
                           :enabled?)]
     (when enable?
       {:fx [[:keychain/save-password-and-auth-method
              {:key-uid         key-uid
               :masked-password (security/mask-data password-hash)
               :on-success      (fn []
                                  (rf/dispatch [:onboarding/set-auth-method
                                                constants/auth-method-biometric]))
               :on-error        #(log/error "failed to save biometrics"
                                            {:key-uid key-uid
                                             :error   %})}]]}))))

(rf/reg-event-fx :onboarding/finish-profile-creation
 (fn [{:keys [db]}]
   (println :onboarding-finished)
   {:db (assoc-in db [:onboarding/replies-by-step :create-profile :loading?] false)}))

(rf/reg-event-fx :onboarding/login
 (fn [_]
   {:fx [[:dispatch [:update-theme-and-init-root :screen/shell-stack]]]}))

(rf/reg-fx :fx.onboarding/create-and-login
 (fn [request]
   (println "create-and-login fx")
   ;;"node.login" signal will be triggered as a callback
   (native-module/create-account-and-login request)))

(rf/reg-event-fx :onboarding/create-profile
 (fn [{:keys [db]}]
   (let [replies      (get db :onboarding/replies-by-step)
         display-name (-> replies :name :input-value)
         color        (-> replies :color :input-value)
         password     (-> replies :create-password :input-value native-module/sha3)]
     (println :args
              (assoc (profile.config/create)
                     :displayName        display-name
                     :password           password
                     :imagePath          nil
                     :customizationColor color))
     {:db (assoc-in db [:onboarding/replies-by-step :create-profile] {:loading? true})
      :fx [[:fx.onboarding/create-and-login
            (assoc (profile.config/create)
                   :displayName        display-name
                   :password           password
                   :imagePath          nil
                   :customizationColor color)]]})))

