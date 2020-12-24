(ns status-im.ui.screens.views
  (:require [status-im.utils.universal-links.core :as utils.universal-links]
            [re-frame.core :as re-frame]
            [cljs-bean.core :refer [bean]]
            [status-im.ui.screens.about-app.views :as about-app]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.routing.core :as navigation]
            [reagent.core :as reagent]
            [status-im.ui.screens.mobile-network-settings.view :as mobile-network-settings]
            [status-im.ui.screens.keycard.views :as keycard]
            [status-im.ui.screens.home.sheet.views :as home.sheet]
            [status-im.ui.screens.routing.main :as routing]
            [status-im.ui.screens.signing.views :as signing]
            [status-im.ui.screens.popover.views :as popover]
            [status-im.ui.screens.multiaccounts.recover.views :as recover.views]
            [status-im.ui.screens.wallet.send.views :as wallet]
            [status-im.ui.components.status-bar.view :as statusbar]
            [status-im.ui.components.colors :as colors]
            [status-im.keycard.test-menu :as keycard.test-menu]
            [quo.core :as quo]
            [status-im.utils.config :as config]
            [status-im.reloader :as reloader]))

(defn on-sheet-cancel []
  (re-frame/dispatch [:bottom-sheet/hide]))

(defn bottom-sheet
  "DEPRECATED: Do not use this place, use event parameters"
  []
  (let [{:keys [show? view]} @(re-frame/subscribe [:bottom-sheet])
        {:keys [content]
         :as   opts}
        (cond-> {:visible?   show?
                 :on-cancel on-sheet-cancel}
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
          (merge recover.views/bottom-sheet))]
    [quo/bottom-sheet opts
     (when content
       [content])]))

(def debug? ^boolean js/goog.DEBUG)

;; Persist navigation state in dev mode
(when debug?
  (defonce state (atom nil))
  (defn- persist-state! [state-obj]
    (js/Promise.
     (fn [resolve _]
       (reset! state state-obj)
       (resolve true)))))

(defn on-state-change [state]
  (let [route-name (navigation/get-active-route-name (bean state))]
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
  (reagent/create-class
   {:component-did-mount    utils.universal-links/initialize
    :component-will-unmount utils.universal-links/finalize
    :reagent-render
    (fn []
      [react/safe-area-provider
       ^{:key (str @colors/theme @reloader/cnt)}
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
         [main-app-navigator]]
        [wallet/select-account]
        [signing/signing]
        [bottom-sheet]
        [popover/popover]
        (when debug?
          [reloader/reload-view @reloader/cnt])
        (when config/keycard-test-menu-enabled?
          [keycard.test-menu/test-menu])]])}))
