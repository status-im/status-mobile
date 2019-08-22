(ns status-im.ui.screens.views
  (:require-macros [status-im.utils.views :refer [defview letsubs] :as views])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.utils.platform :refer [android?]]
            [status-im.utils.universal-links.core :as utils.universal-links]
            [status-im.ui.screens.about-app.views :as about-app]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.bottom-sheet.core :as bottom-sheet]
            [status-im.utils.navigation :as navigation]
            [reagent.core :as reagent]
            [status-im.utils.random :as rand]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.mobile-network-settings.view :as mobile-network-settings]
            [status-im.ui.screens.keycard.views :as keycard]
            [status-im.ui.screens.home.sheet.views :as home.sheet]
            [status-im.ui.screens.routing.core :as routing]
            [status-im.ui.screens.signing.views :as signing]
            [status-im.ui.screens.popover.views :as popover]
            [status-im.ui.screens.multiaccounts.recover.views :as recover.views]
            [status-im.utils.dimensions :as dimensions]
            status-im.ui.screens.wallet.collectibles.etheremon.views
            status-im.ui.screens.wallet.collectibles.cryptostrikers.views
            status-im.ui.screens.wallet.collectibles.cryptokitties.views
            status-im.ui.screens.wallet.collectibles.superrare.views
            status-im.ui.screens.wallet.collectibles.kudos.views))

(defonce rand-label (when js/goog.DEBUG (rand/id)))

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

                 (= view :learn-more)
                 (merge about-app/learn-more)

                 (= view :keycard.login/more)
                 (merge keycard/more-sheet)

                 (= view :private-chat-actions)
                 (merge home.sheet/private-chat-actions)

                 (= view :group-chat-actions)
                 (merge home.sheet/group-chat-actions)

                 (= view :recover-sheet)
                 (merge recover.views/bottom-sheet))]

      [bottom-sheet/bottom-sheet opts])))

(defn reset-component-on-mount [view-id component two-pane?]
  (when (and @initial-view-id
             (or
              js/goog.DEBUG
              (not @component)))
    (reset! component (routing/get-main-component
                       (if js/goog.DEBUG
                         @initial-view-id
                         @view-id)
                       two-pane?))))

(defn reset-component-on-update [view-id component two-pane?]
  (when (and @initial-view-id (not @component))
    (reset! component (routing/get-main-component
                       (if js/goog.DEBUG
                         @initial-view-id
                         @view-id)
                       two-pane?))))

(defonce state (atom nil))

(defn persist-state [state-obj]
  (js/Promise.
   (fn [resolve reject]
     (reset! state state-obj)
     (resolve true))))

(defn load-state []
  (js/Promise.
   (fn [resolve reject]
     (resolve @state))))

(defn main []
  (let [view-id                 (re-frame/subscribe [:view-id])
        main-component          (atom nil)
        main-component-two-pane (atom nil)
        two-pane?               (reagent/atom (dimensions/fit-two-pane?))]
    (reagent/create-class
     {:component-did-mount
      (fn []
        (.addEventListener (react/dimensions)
                           "change"
                           (fn [dimensions]
                             (let [two-pane-enabled? (dimensions/fit-two-pane?)]
                               (do
                                 (re-frame/dispatch [:set-two-pane-ui-enabled two-pane-enabled?])
                                 (log/debug ":set-two-pane " two-pane-enabled?)
                                 (reset! two-pane? two-pane-enabled?)))))

        (when-not @initial-view-id
          (reset! initial-view-id @view-id))
        (reset-component-on-mount view-id main-component false)
        (reset-component-on-mount view-id main-component-two-pane true)
        (re-frame/dispatch [:set-two-pane-ui-enabled @two-pane?])
        (log/debug :main-component-did-mount @view-id)
        (utils.universal-links/initialize))
      :component-will-unmount
      utils.universal-links/finalize
      :component-will-update
      (fn []
        (when-not @initial-view-id
          (reset! initial-view-id @view-id))

        (reset-component-on-update view-id main-component false)
        (reset-component-on-update view-id main-component-two-pane true)
        (when-not platform/desktop?
          (react/dismiss-keyboard!)))
      :component-did-update
      (fn []
        (log/debug :main-component-did-update @view-id))
      :reagent-render
      (fn []
        (when (and @view-id main-component)
          [react/view {:flex 1}
           [:> (if @two-pane? @main-component-two-pane @main-component)
            {:ref                    (fn [r]
                                       (navigation/set-navigator-ref r)
                                       (when (and
                                              platform/android?
                                              (not js/goog.DEBUG)
                                              (not (contains? #{:intro :login :progress} @view-id)))
                                         (navigation/navigate-to @view-id nil)))
              ;; see https://reactnavigation.org/docs/en/state-persistence.html#development-mode
             :persistNavigationState (when js/goog.DEBUG persist-state)
             :loadNavigationState    (when js/goog.DEBUG load-state)}]
           [signing/signing]
           [bottom-sheet]
           [popover/popover]]))})))
