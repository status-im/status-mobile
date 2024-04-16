(ns status-im.navigation.view
  (:require
    [legacy.status-im.bottom-sheet.sheets :as bottom-sheets-old]
    [legacy.status-im.ui.screens.popover.views :as popover]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    schema.view
    [status-im.common.alert-banner.view :as alert-banner]
    [status-im.common.bottom-sheet-screen.view :as bottom-sheet-screen]
    [status-im.common.bottom-sheet.view :as bottom-sheet]
    [status-im.common.toasts.view :as toasts]
    [status-im.navigation.screens :as screens]
    [status-im.setup.hot-reload :as reloader]
    [utils.re-frame :as rf]))

(def functional-compiler (reagent/create-compiler {:function-components true}))

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
  [{:keys [top? bottom? background-color alert-banners-top-margin]}]
  (merge
   {:flex             1
    :margin-top       alert-banners-top-margin
    :background-color (or background-color (colors/theme-colors colors/white colors/neutral-100))}
   (when bottom?
     {:padding-bottom (safe-area/get-bottom)})
   (when top?
     {:padding-top (safe-area/get-top)})))

(defn screen
  [screen-key]
  (reagent.core/reactify-component
   (fn []
     (let [screen-details              (get (if js/goog.DEBUG
                                              (get-screens)
                                              screens)
                                            (keyword screen-key))
           qualified-screen-details    (get (if js/goog.DEBUG
                                              (get-screens)
                                              screens)
                                            (keyword "screen" screen-key))
           {:keys [component options]} (or qualified-screen-details screen-details)
           {:keys [insets sheet? theme
                   skip-background?]}  options
           user-theme                  (quo.theme/get-theme)
           alert-banners-top-margin    (rf/sub [:alert-banners/top-margin])
           background-color            (or (get-in options [:layout :backgroundColor])
                                           (when sheet? :transparent))]
       ^{:key (str "root" screen-key @reloader/cnt)}
       [quo.theme/provider (or theme user-theme)
        [rn/view
         {:style (wrapped-screen-style (assoc
                                        insets
                                        :background-color         background-color
                                        :alert-banners-top-margin alert-banners-top-margin))}
         [inactive]
         (if sheet?
           [bottom-sheet-screen/view {:content component :skip-background? skip-background?}]
           [component])]
        (when js/goog.DEBUG
          [:<>
           [reloader/reload-view]
           [schema.view/view]])]))
   functional-compiler))

(def bottom-sheet
  (reagent/reactify-component
   (fn []
     (let [{:keys [sheets hide?]}   (rf/sub [:bottom-sheet])
           sheet                    (last sheets)
           {:keys [theme]}          sheet
           insets                   (safe-area/get-insets)
           user-theme               (quo.theme/get-theme)
           keyboard-vertical-offset (- (max 20 (:bottom insets)))]
       ^{:key (str "sheet" @reloader/cnt)}
       [quo.theme/provider (or theme user-theme)
        [inactive]
        [rn/keyboard-avoiding-view
         {:style                    {:position :relative :flex 1}
          :keyboard-vertical-offset keyboard-vertical-offset}
         (when sheet
           [bottom-sheet/view
            {:insets insets :hide? hide?}
            sheet])]]))
   functional-compiler))

(def toasts (reagent/reactify-component toasts/toasts functional-compiler))

(def alert-banner
  (reagent/reactify-component
   (fn []
     ^{:key (str "alert-banner" @reloader/cnt)}
     [quo.theme/provider :dark
      [alert-banner/view]])
   functional-compiler))

;; LEGACY (should be removed in status 2.0)
(def sheet-comp-old
  (reagent/reactify-component
   (fn []
     ^{:key (str "sheet-old" @reloader/cnt)}
     [:<>
      [inactive]
      [bottom-sheets-old/bottom-sheet]])
   functional-compiler))

(def popover-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "popover" @reloader/cnt)}
     [:<>
      [inactive]
      [popover/popover]
      (when js/goog.DEBUG
        [reloader/reload-view])])
   functional-compiler))
