(ns status-chat.events
  (:require [native-module.core :as native-module]
            [promesa.core :as promesa]
            [re-frame.core :as rf]
            [status-chat.db :as db]
            [status-chat.init :as init]
            status-chat.onboarding.events
            [status-chat.profile.core :as profile]
            status-im.common.signals.events
            [taoensso.timbre :as log]
            [utils.transforms :as transforms]))

(rf/reg-event-fx :app/init
 (fn [_]
   {:db db/app-db
    :fx [[:effects.app/get-profiles
          {:on-success #(rf/dispatch [:app/init-profiles %])}]
         #_[:effects.app/register-signals-handler
            #(rf/dispatch [:signals/signal-received %])]]}))

(rf/reg-fx :effects.app/get-profiles
 (fn [{:keys [on-success on-error]}]
   (-> (init/get-profiles)
       (promesa/then on-success)
       (promesa/catch on-error))))

(rf/reg-event-fx :app/init-profiles
 (fn [{:keys [db]} [profiles]]
   (let [selected-profile (some-> profiles
                                  profile/recently-opened
                                  profile/key-uid)]
     #_(if selected-profile
         {:db (assoc db
                     :profiles/by-id    profiles
                     :profiles/selected selected-profile)
          :fx [[
                ;; TODO show the auth screen
                ;; TODO biometric auth
               ]]}
         {:fx [
               [:dispatch [:onboarding/start]]
              ]})
     {:fx [[:dispatch [:onboarding/start]]]}
   )))

(rf/reg-fx :effects.app/register-signals-handler
 (fn [handler]
   (native-module/init handler)))

(rf/reg-event-fx :app/handle-signal
 (fn [{:keys [db]} [event-str]]
   (let [^js data     (.parse js/JSON event-str)
         ^js event-js (.-event data)
         type         (.-type data)
         event        (transforms/js->clj event-js)]
     (log/debug "Signal received" {:type type})
     (log/trace "Signal received" {:payload event}))))
