(ns status-im2.navigation.view
  (:require [quo2.foundations.colors :as colors]
            [re-frame.core :as re-frame]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im.keycard.test-menu :as keycard.test-menu]
            [status-im2.common.bottom-sheet.sheets :as bottom-sheets]
            [status-im.ui.screens.popover.views :as popover]
            [status-im.ui.screens.profile.visibility-status.views :as visibility-status-views]
            [status-im.ui.screens.signing.views :as signing]
            [status-im.ui.screens.wallet-connect.session-proposal.views :as wallet-connect]
            [status-im.ui.screens.wallet.send.views :as wallet.send.views]
            [status-im2.common.toasts.view :as toasts]
            [status-im2.navigation.screens :as screens]
            [status-im2.config :as config]
            [status-im2.setup.hot-reload :as reloader]))

(defn get-screens
  []
  (reduce
   (fn [acc screen]
     (assoc acc (:name screen) screen))
   {}
   (screens/screens)))

;;we need this for hot reload (for some reason it doesn't reload, so we have to call get-screens if
;;debug
;;true)
(def screens (get-screens))

(def components
  (reduce
   (fn [acc {:keys [name component]}]
     (assoc acc name component))
   {}
   (concat screens/components)))

(defn wrapped-screen-style
  [insets-options insets {:keys [background-color]}]
  (merge
   {:flex             1
    :background-color (or background-color (colors/theme-colors colors/white colors/neutral-100))}
   (when (get insets-options :bottom)
     {:padding-bottom (:bottom insets)})
   (when (get insets-options :top true)
     {:padding-top (:top insets)})))

(defn inactive
  []
  (when @(re-frame/subscribe [:hide-screen?])
    [rn/view
     {:position         :absolute
      :flex             1
      :top              0
      :bottom           0
      :left             0
      :right            0
      :background-color (colors/theme-colors colors/white colors/neutral-100)
      :z-index          999999999999999999}]))

(defn screen
  [key]
  (reagent.core/reactify-component
   (fn []
     (let [{:keys [component insets component-options]}
           (get (if js/goog.DEBUG (get-screens) screens) (keyword key))]
       ^{:key (str "root" key @reloader/cnt)}
       [safe-area/provider
        [safe-area/consumer
         (fn [safe-insets]
           [rn/view
            {:style (wrapped-screen-style insets safe-insets component-options)}
            [inactive]
            [component]])]
        (when js/goog.DEBUG
          [reloader/reload-view])]))))

(defn component
  [comp]
  (reagent/reactify-component
   (fn []
     [rn/view {:width 500 :height 44}
      [comp]])))

(def popover-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "popover" @reloader/cnt)}
     [safe-area/provider
      [inactive]
      [popover/popover]
      (when js/goog.DEBUG
        [reloader/reload-view])])))

(def toasts-comp
  (reagent/reactify-component
   (fn []
     ;; DON'T wrap this in safe-area-provider, it makes it unable to click through toasts
     ^{:key (str "toasts" @reloader/cnt)}
     [toasts/toasts])))

(def visibility-status-popover-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "visibility-status-popover" @reloader/cnt)}
     [safe-area/provider
      [inactive]
      [visibility-status-views/visibility-status-popover]
      (when js/goog.DEBUG
        [reloader/reload-view])])))

(def sheet-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "sheet" @reloader/cnt)}
     [safe-area/provider
      [inactive]
      [bottom-sheets/bottom-sheet]
      (when js/goog.DEBUG
        [reloader/reload-view])
      (when config/keycard-test-menu-enabled?
        [keycard.test-menu/test-menu])])))

(def signing-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "signing-sheet" @reloader/cnt)}
     [safe-area/provider
      [inactive]
      [signing/signing]
      (when js/goog.DEBUG
        [reloader/reload-view])])))

(def select-acc-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "select-acc-sheet" @reloader/cnt)}
     [safe-area/provider
      [inactive]
      [wallet.send.views/select-account]
      (when js/goog.DEBUG
        [reloader/reload-view])])))

(def wallet-connect-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "wallet-connect-sheet" @reloader/cnt)}
     [safe-area/provider
      [inactive]
      [wallet-connect/wallet-connect-proposal-sheet]
      (when js/goog.DEBUG
        [reloader/reload-view])])))

(def wallet-connect-success-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "wallet-connect-success-sheet" @reloader/cnt)}
     [safe-area/provider
      [inactive]
      [wallet-connect/wallet-connect-success-sheet-view]
      (when js/goog.DEBUG
        [reloader/reload-view])])))

(def wallet-connect-app-management-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "wallet-connect-app-management-sheet" @reloader/cnt)}
     [safe-area/provider
      [inactive]
      [wallet-connect/wallet-connect-app-management-sheet-view]
      (when js/goog.DEBUG
        [reloader/reload-view])])))
