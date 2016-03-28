(ns syng-im.android.core
  (:require-macros
    [natal-shell.back-android :refer [add-event-listener remove-event-listener]])
  (:require [reagent.core :as r :refer [atom]]
            [cljs.core :as cljs]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.handlers]
            [syng-im.subs]
            [syng-im.components.react :refer [navigator app-registry]]
            [syng-im.components.contact-list.contact-list :refer [contact-list]]
            [syng-im.components.chat :refer [chat]]
            [syng-im.components.login :refer [login-view]]

            ;; [syng-im.components.chat.chat :refer [chat]]
            [syng-im.components.nav :as nav]
            [syng-im.utils.logging :as log]))

;; (def ^{:dynamic true :private true} *nav-render*
;;   "Flag to suppress navigator re-renders from outside om when pushing/popping."
;;   true)

(def back-button-handler (cljs/atom {:nav     nil
                                     :handler nil}))

(defn init-back-button-handler! [nav]
  (let [handler @back-button-handler]
    (when-not (= nav (:nav handler))
      (remove-event-listener "hardwareBackPress" (:handler handler))
      (let [new-listener (fn []
                           (binding [nav/*nav-render* false]
                             (when (< 1 (.-length (.getCurrentRoutes nav)))
                               (.pop nav)
                               true)))]
        (reset! back-button-handler {:nav     nav
                                     :handler new-listener})
        (add-event-listener "hardwareBackPress" new-listener)))))

(defn app-root []
  [navigator {:initial-route (clj->js {:view-id ;; :chat
                                       ;; :contact-list
                                       :login
                                       })
              :render-scene  (fn [route nav]
                               (log/debug "route" route)
                               (when nav/*nav-render*
                                 (let [{:keys [view-id]} (js->clj route :keywordize-keys true)
                                       view-id (keyword view-id)]
                                   (init-back-button-handler! nav)
                                   (case view-id
                                     :contact-list (r/as-element [contact-list
                                                                  {:navigator nav}])
                                     :chat (r/as-element [chat {:navigator nav}])
                                     :login (r/as-element [login-view {:navigator nav}])))))}])

(defn init []
  (dispatch-sync [:initialize-db])
  (dispatch [:initialize-protocol])
  (dispatch [:load-user-phone-number])
  (dispatch [:load-syng-contacts])
  (.registerComponent app-registry "SyngIm" #(r/reactify-component app-root)))
