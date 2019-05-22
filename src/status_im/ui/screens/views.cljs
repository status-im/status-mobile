(ns status-im.ui.screens.views
  (:require-macros [status-im.utils.views :refer [defview letsubs] :as views])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.utils.platform :refer [android?]]
            [status-im.utils.universal-links.core :as utils.universal-links]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.bottom-sheet.core :as bottom-sheet]
            [status-im.utils.navigation :as navigation]
            [reagent.core :as reagent]
            [status-im.utils.random :as rand]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.mobile-network-settings.view :as mobile-network-settings]
            [status-im.ui.screens.home.sheet.views :as home.sheet]
            [status-im.ui.screens.routing.core :as routing]))

(defonce rand-label (rand/id))

(defonce initial-view-id (atom nil))

(views/defview bottom-sheet []
  (views/letsubs [{:keys [show? view]} [:bottom-sheet]]
    (let [opts (cond-> {:show?     show?
                        :on-cancel #(re-frame/dispatch [:bottom-sheet/hide])}

                 (map? view)
                 (merge view)

                 (= view :mobile-network)
                 (merge mobile-network-settings/settings-sheet)

                 (= view :mobile-network-offline)
                 (merge mobile-network-settings/offline-sheet)

                 (= view :add-new)
                 (merge home.sheet/add-new)

                 (= view :public-chat-actions)
                 (merge home.sheet/public-chat-actions)

                 (= view :private-chat-actions)
                 (merge home.sheet/private-chat-actions)

                 (= view :group-chat-actions)
                 (merge home.sheet/group-chat-actions))]

      [bottom-sheet/bottom-sheet opts])))

(defn main []
  (let [view-id        (re-frame/subscribe [:view-id])
        main-component (atom nil)]
    (reagent/create-class
     {:component-did-mount
      (fn []
        (log/debug :main-component-did-mount @view-id)
        (utils.universal-links/initialize))
      :component-will-mount
      (fn []
        (when-not @initial-view-id
          (reset! initial-view-id @view-id))
        (when (and @initial-view-id
                   (or
                    js/goog.DEBUG
                    (not @main-component)))
          (reset! main-component (routing/get-main-component
                                  (if js/goog.DEBUG
                                    @initial-view-id
                                    @view-id)))))
      :component-will-unmount
      utils.universal-links/finalize
      :component-will-update
      (fn []
        (when-not @initial-view-id
          (reset! initial-view-id @view-id))
        (when (and @initial-view-id (not @main-component))
          (reset! main-component (routing/get-main-component
                                  (if js/goog.DEBUG
                                    @initial-view-id
                                    @view-id))))
        (when-not platform/desktop?
          (react/dismiss-keyboard!)))
      :component-did-update
      (fn []
        (log/debug :main-component-did-update @view-id))
      :reagent-render
      (fn []
        (when (and @view-id main-component)
          [react/view {:flex 1}
           [:> @main-component
            {:ref            (fn [r]
                               (navigation/set-navigator-ref r)
                               (when (and
                                      platform/android?
                                      (not js/goog.DEBUG)
                                      (not (contains? #{:intro :login :progress} @view-id)))
                                 (navigation/navigate-to @view-id nil)))
             ;; see https://reactnavigation.org/docs/en/state-persistence.html#development-mode
             :persistenceKey (when js/goog.DEBUG rand-label)}]
           [bottom-sheet]]))})))

