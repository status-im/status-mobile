(ns status-im2.navigation.view
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im.bottom-sheet.sheets :as bottom-sheets-old]
            [status-im2.common.bottom-sheet.view :as bottom-sheet]
            [status-im.ui.screens.popover.views :as popover]
            [status-im.ui.screens.profile.visibility-status.views :as visibility-status-views]
            [status-im.ui.screens.signing.views :as signing]
            [status-im.ui.screens.wallet-connect.session-proposal.views :as wallet-connect]
            [status-im.ui.screens.wallet.send.views :as wallet.send.views]
            [status-im2.common.toasts.view :as toasts]
            [status-im2.navigation.screens :as screens]
            [status-im2.setup.hot-reload :as reloader]
            [utils.re-frame :as rf]
            [status-im2.common.bottom-sheet-screen.view :as bottom-sheet-screen]))

(defn get-screens
  []
  (reduce
   (fn [acc screen]
     (assoc acc (:name screen) screen))
   {}
   (screens/screens)))

(def screens (get-screens))

(defn inactive
  []
  (when (rf/sub [:hide-screen?])
    [rn/view
     {:position         :absolute
      :flex             1
      :top              0
      :bottom           0
      :left             0
      :right            0
      :background-color (colors/theme-colors colors/white colors/neutral-100)
      :z-index          999999999999999999}]))

(defn wrapped-screen-style
  [{:keys [top? bottom?]} insets background-color]
  (merge
   {:flex             1
    :background-color (or background-color (colors/theme-colors colors/white colors/neutral-100))}
   (when bottom?
     {:padding-bottom (:bottom insets)})
   (when top?
     {:padding-top (:top insets)})))

(defn screen
  [key]
  (reagent.core/reactify-component
   (fn []
     (let [{:keys [component options]}
           (get (if js/goog.DEBUG (get-screens) screens) (keyword key)) ;; needed for hot reload
           {:keys [insets sheet?]} options
           background-color (or (get-in options [:layout :backgroundColor])
                                (when sheet? :transparent))]
       ^{:key (str "root" key @reloader/cnt)}
       [safe-area/provider
        [safe-area/consumer
         (fn [safe-insets]
           [rn/view
            {:style (wrapped-screen-style insets safe-insets background-color)}
            [inactive]
            (if sheet?
              [bottom-sheet-screen/view component]
              [component])])]
        (when js/goog.DEBUG
          [reloader/reload-view])]))))

(def bottom-sheet
  (reagent/reactify-component
   (fn []
     ^{:key (str "sheet" @reloader/cnt)}
     [safe-area/provider
      [inactive]
      [safe-area/consumer
       (fn [insets]
         (let [{:keys [sheets hide?]} (rf/sub [:bottom-sheet])
               sheet                  (last sheets)]
           [rn/keyboard-avoiding-view
            {:style                    {:position :relative :flex 1}
             :keyboard-vertical-offset (- (max 20 (:bottom insets)))}
            (when sheet
              [:f>
               bottom-sheet/view
               {:insets insets :hide? hide?}
               sheet])]))]])))

(def toasts (reagent/reactify-component toasts/toasts))

;; LEGACY (should be removed in status 2.0)
(def popover-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "popover" @reloader/cnt)}
     [safe-area/provider
      [inactive]
      [popover/popover]
      (when js/goog.DEBUG
        [reloader/reload-view])])))

(def visibility-status-popover-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "visibility-status-popover" @reloader/cnt)}
     [safe-area/provider
      [inactive]
      [visibility-status-views/visibility-status-popover]
      (when js/goog.DEBUG
        [reloader/reload-view])])))

(def sheet-comp-old
  (reagent/reactify-component
   (fn []
     ^{:key (str "sheet-old" @reloader/cnt)}
     [safe-area/provider
      [inactive]
      [bottom-sheets-old/bottom-sheet]])))

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
