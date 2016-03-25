(ns syng-im.android.core
  (:require-macros
    [natal-shell.back-android :refer [add-event-listener remove-event-listener]])
  (:require [reagent.core :as r :refer [atom]]
            [cljs.core :as cljs]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.handlers]
            [syng-im.subs]
            [syng-im.components.react :refer [navigator app-registry]]
            [syng-im.components.chat :refer [chat]]
            [syng-im.utils.logging :as log]
            [syng-im.navigation :refer [*nav-render*]]))


(def back-button-handler (cljs/atom {:nav     nil
                                     :handler nil}))

(defn init-back-button-handler! [nav]
  (let [handler @back-button-handler]
    (when-not (= nav (:nav handler))
      (remove-event-listener "hardwareBackPress" (:handler handler))
      (let [new-listener (fn []
                           (binding [*nav-render* false]
                             (when (< 1 (.-length (.getCurrentRoutes nav)))
                               (.pop nav)
                               true)))]
        (reset! back-button-handler {:nav     nav
                                     :handler new-listener})
        (add-event-listener "hardwareBackPress" new-listener)))))

(defn app-root []
  [navigator {:initial-route (clj->js {:view-id :chat})
              :render-scene  (fn [route nav]
                               (log/debug "route" route)
                               (when *nav-render*
                                 (let [{:keys [view-id]} (js->clj route :keywordize-keys true)
                                       view-id (keyword view-id)]
                                   (init-back-button-handler! nav)
                                   (case view-id
                                     :chat (r/as-element [chat {:navigator nav}])))))}])

(defn init []
  (dispatch-sync [:initialize-db])
  (dispatch [:initialize-protocol])
  (.registerComponent app-registry "SyngIm" #(r/reactify-component app-root)))
