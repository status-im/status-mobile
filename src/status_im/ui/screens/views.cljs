(ns status-im.ui.screens.views
  (:require-macros [status-im.utils.views :refer [defview letsubs] :as views])
  (:require [status-im.utils.universal-links.core :as utils.universal-links]
            [re-frame.core :as re-frame]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.about-app.views :as about-app]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.bottom-sheet.core :as bottom-sheet]
            [status-im.ui.screens.routing.core :as navigation]
            [reagent.core :as reagent]
            [status-im.ui.screens.mobile-network-settings.view :as mobile-network-settings]
            [status-im.ui.screens.keycard.views :as keycard]
            [status-im.ui.screens.home.sheet.views :as home.sheet]
            [status-im.ui.screens.routing.main :as routing]
            [status-im.ui.screens.signing.views :as signing]
            [status-im.ui.screens.popover.views :as popover]
            [status-im.ui.screens.multiaccounts.recover.views :as recover.views]
            [status-im.utils.dimensions :as dimensions]
            [status-im.ui.screens.wallet.send.views :as wallet]
            [status-im.ui.components.tabbar.core :as tabbar]
            [status-im.ui.components.status-bar.view :as statusbar]
            status-im.ui.screens.wallet.collectibles.etheremon.views
            status-im.ui.screens.wallet.collectibles.cryptostrikers.views
            status-im.ui.screens.wallet.collectibles.cryptokitties.views
            status-im.ui.screens.wallet.collectibles.superrare.views
            status-im.ui.screens.wallet.collectibles.kudos.views
            [status-im.ui.components.colors :as colors]))

(defview bottom-sheet []
  (letsubs [{:keys [show? view]} [:bottom-sheet]]
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

                 (= view :keycard.login/more)
                 (merge keycard/more-sheet)

                 (= view :learn-more)
                 (merge about-app/learn-more)

                 (= view :recover-sheet)
                 (merge (recover.views/bottom-sheet)))]
      [bottom-sheet/bottom-sheet opts])))

(def debug? ^boolean js/goog.DEBUG)

;; Persist navigation state in dev mode
(when debug?
  (defonce state (atom nil))
  (defn- persist-state! [state-obj]
    (js/Promise.
     (fn [resolve _]
       (reset! state state-obj)
       (resolve true)))))

(defn get-active-route-name [state]
  (let [index (get state "index")
        route (get-in state ["routes" index])]
    (if-let [state' (get route "state")]
      (get-active-route-name state')
      (some-> (get route "name") keyword))))

(defn on-state-change [state]
  (let [route-name (get-active-route-name (js->clj state))]
    (tabbar/minimize-bar route-name)

    ;; NOTE(Ferossgp): Keycard did-load events backward compatibility
    (re-frame/dispatch [:screens/on-will-focus route-name])

    ;; NOTE(Ferossgp): Both calls are for backward compatibility, should be reworked in future
    (statusbar/set-status-bar route-name)
    (re-frame/dispatch [:set :view-id route-name]))
  (when debug?
    (persist-state! state)))

(defonce main-app-navigator    (partial routing/get-main-component false))
(defonce twopane-app-navigator (partial routing/get-main-component true))

(defn main []
  (let [two-pane? (reagent/atom (dimensions/fit-two-pane?))]
    (.addEventListener react/dimensions
                       "change"
                       (fn [_]
                         (let [two-pane-enabled? (dimensions/fit-two-pane?)]
                           (re-frame/dispatch [:set-two-pane-ui-enabled two-pane-enabled?])
                           (reset! two-pane? two-pane-enabled?))))
    (reagent/create-class
     {:component-did-mount
      (fn []
        (re-frame/dispatch [:set-two-pane-ui-enabled @two-pane?])
        (utils.universal-links/initialize))

      :component-will-unmount
      utils.universal-links/finalize

      :reagent-render
      (fn []
        [react/safe-area-provider
         ^{:key @colors/theme}
         [react/view {:flex             1
                      :background-color colors/black-persist}
          [navigation/navigation-container
           (merge {:ref               (fn [r]
                                        (navigation/set-navigator-ref r))
                   :onStateChange     on-state-change
                   :enableURLHandling false}
                  (when debug?
                    {:enableURLHandling true
                     :initialState      @state}))
           [(if @two-pane? twopane-app-navigator main-app-navigator)]]
          [wallet/prepare-transaction]
          [wallet/request-transaction]
          [wallet/select-account]
          [signing/signing]
          [bottom-sheet]
          [popover/popover]]])})))
