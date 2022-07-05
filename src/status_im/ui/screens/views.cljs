(ns status-im.ui.screens.views
  (:require [status-im.ui.components.react :as react]
            [reagent.core :as reagent]
            [status-im.reloader :as reloader]
            [status-im.ui.screens.screens :as screens]
            [oops.core :refer [oget]]
            [status-im.ui.screens.popover.views :as popover]
            [status-im.ui.screens.profile.visibility-status.views :as visibility-status-views]
            [status-im.ui.screens.bottom-sheets.views :as bottom-sheets]
            [status-im.ui.screens.signing.views :as signing]
            [status-im.ui.screens.wallet.send.views :as wallet.send.views]
            [re-frame.core :as re-frame]
            [quo.design-system.colors :as colors]
            [status-im.utils.config :as config]
            [status-im.keycard.test-menu :as keycard.test-menu]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.wallet-connect.session-proposal.views :as wallet-connect]))

(defn get-screens []
  (reduce
   (fn [acc screen]
     (assoc acc (:name screen) screen))
   {}
   (screens/screens)))

;;we need this for hot reload (for some reason it doesn't reload, so we have to call get-screens if debug true)
(def screens (get-screens))

(def components
  (reduce
   (fn [acc {:keys [name component]}]
     (assoc acc name component))
   {}
   (concat screens/components)))

(defn wrapped-screen-style [{:keys [insets style]} insets-obj]
  (merge
   {:flex 1
    :background-color colors/white}
   (when platform/android?
     {:border-bottom-width 1
      :border-bottom-color colors/gray-lighter})
   style
   (when (get insets :bottom)
     {:padding-bottom (+ (oget insets-obj "bottom")
                         (get style :padding-bottom)
                         (get style :padding-vertical))})
   (when (get insets :top true)
     {:padding-top (+ (oget insets-obj "top")
                      (get style :padding-top)
                      (get style :padding-vertical))})))

(defn inactive []
  (when @(re-frame/subscribe [:hide-screen?])
    [react/view {:position :absolute :flex 1 :top 0 :bottom 0 :left 0 :right 0 :background-color colors/white
                 :z-index 999999999999999999}]))

(defn screen [key]
  (reagent.core/reactify-component
   (fn []
     (let [{:keys [component insets]} (get
                                       (if (or js/goog.DEBUG @config/new-ui-enabled?)
                                         (get-screens)
                                         screens)
                                       (keyword key))]
       ^{:key (str "root" key @reloader/cnt)}
       [react/safe-area-provider
        [react/safe-area-consumer
         (fn [safe-insets]
           (reagent/as-element
            [react/view {:style (wrapped-screen-style
                                 {:insets insets}
                                 safe-insets)}
             [inactive]
             [component]]))]
        (when js/goog.DEBUG
          [reloader/reload-view])]))))

(defn component [comp]
  (reagent/reactify-component
   (fn []
     [react/view {:width 500 :height 44}
      [comp]])))

(def popover-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "popover" @reloader/cnt)}
     [react/safe-area-provider
      [inactive]
      [popover/popover]
      (when js/goog.DEBUG
        [reloader/reload-view])])))

(def visibility-status-popover-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "visibility-status-popover" @reloader/cnt)}
     [react/safe-area-provider
      [inactive]
      [visibility-status-views/visibility-status-popover]
      (when js/goog.DEBUG
        [reloader/reload-view])])))

(def sheet-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "seet" @reloader/cnt)}
     [react/safe-area-provider
      [inactive]
      [bottom-sheets/bottom-sheet]
      (when js/goog.DEBUG
        [reloader/reload-view])
      (when config/keycard-test-menu-enabled?
        [keycard.test-menu/test-menu])])))

(def sheet-comp-redesign
  (reagent/reactify-component
   (fn []
     ^{:key (str "seet" @reloader/cnt)}
     [react/safe-area-provider
      [inactive]
      [bottom-sheets/bottom-sheet-redesign]
      (when js/goog.DEBUG
        [reloader/reload-view])
      (when config/keycard-test-menu-enabled?
        [keycard.test-menu/test-menu])])))

(def signing-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "signing-sheet" @reloader/cnt)}
     [react/safe-area-provider
      [inactive]
      [signing/signing]
      (when js/goog.DEBUG
        [reloader/reload-view])])))

(def select-acc-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "select-acc-sheet" @reloader/cnt)}
     [react/safe-area-provider
      [inactive]
      [wallet.send.views/select-account]
      (when js/goog.DEBUG
        [reloader/reload-view])])))

(def wallet-connect-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "wallet-connect-sheet" @reloader/cnt)}
     [react/safe-area-provider
      [inactive]
      [wallet-connect/wallet-connect-proposal-sheet]
      (when js/goog.DEBUG
        [reloader/reload-view])])))

(def wallet-connect-success-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "wallet-connect-success-sheet" @reloader/cnt)}
     [react/safe-area-provider
      [inactive]
      [wallet-connect/wallet-connect-success-sheet-view]
      (when js/goog.DEBUG
        [reloader/reload-view])])))

(def wallet-connect-app-management-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "wallet-connect-app-management-sheet" @reloader/cnt)}
     [react/safe-area-provider
      [inactive]
      [wallet-connect/wallet-connect-app-management-sheet-view]
      (when js/goog.DEBUG
        [reloader/reload-view])])))
