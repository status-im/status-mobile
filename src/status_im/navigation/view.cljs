(ns status-im.navigation.view
  (:require
    [legacy.status-im.ui.screens.popover.views :as popover]
    [quo.context]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [react-native.safe-area-context :as safe-area-context]
    [reagent.core :as reagent]
    schema.view
    [status-im.common.alert-banner.view :as alert-banner]
    [status-im.common.bottom-sheet-screen.view :as bottom-sheet-screen]
    [status-im.common.bottom-sheet.view :as bottom-sheet]
    [status-im.common.toasts.view :as toasts]
    [status-im.contexts.keycard.nfc.sheets.view :as keycard.sheet]
    [status-im.navigation.screens :as screens]
    [status-im.setup.hot-reload :as reloader]
    [utils.re-frame :as rf]))

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
  (let [theme (rf/sub [:theme])]
    (when (rf/sub [:hide-screen?])
      [rn/view
       {:position         :absolute
        :flex             1
        :top              0
        :bottom           0
        :left             0
        :right            0
        :background-color (colors/theme-colors colors/white colors/neutral-100 theme)
        :z-index          999999999999999999}])))

(defn wrapped-screen-style
  [{:keys [top? bottom? background-color alert-banners-top-margin theme]}]
  (merge
   {:flex             1
    :margin-top       alert-banners-top-margin
    :background-color (or background-color (colors/theme-colors colors/white colors/neutral-95 theme))}
   (when bottom?
     {:padding-bottom safe-area/bottom})
   (when top?
     {:padding-top safe-area/top})))

(defn screen
  [screen-key]
  (reagent.core/reactify-component
   (fn []
     (let [screen-details                   (get (if js/goog.DEBUG
                                                   (get-screens)
                                                   screens)
                                                 (keyword "screen" screen-key))
           {:keys [component options name]} screen-details
           screen-params                    (rf/sub [:get-screen-params name])
           {:keys [insets sheet? theme
                   skip-background?]}       options
           alert-banners-top-margin         (rf/sub [:alert-banners/top-margin])
           background-color                 (or (get-in options [:layout :backgroundColor])
                                                (when sheet? :transparent))
           app-theme                        (rf/sub [:theme])
           theme                            (or theme app-theme)]
       ^{:key (str "root" screen-key @reloader/cnt)}
       [quo.context/provider {:theme theme :screen-id name :screen-params screen-params}
        [safe-area-context/safe-area-provider
         [rn/view
          {:style (wrapped-screen-style (assoc
                                         insets
                                         :theme                    theme
                                         :background-color         background-color
                                         :alert-banners-top-margin alert-banners-top-margin))}
          [inactive]
          (if sheet?
            [bottom-sheet-screen/view {:content component :skip-background? skip-background?}]
            [component])]]
        (when js/goog.DEBUG
          [:<>
           [reloader/reload-view]
           [schema.view/view]])]))))

(def bottom-sheet
  (reagent/reactify-component
   (fn []
     (let [app-theme                 (rf/sub [:theme])
           view-id                   (rf/sub [:view-id])
           {:keys [sheets hide?]}    (rf/sub [:bottom-sheet])
           sheet                     (last sheets)
           {:keys [theme screen-id]} sheet
           screen-id-value           (or screen-id view-id)
           screen-params             (rf/sub [:get-screen-params screen-id-value])
           insets                    safe-area/insets
           keyboard-vertical-offset  (- (max 20 (:bottom insets)))]
       ^{:key (str "sheet" @reloader/cnt)}
       [quo.context/provider
        {:theme         (or theme app-theme)
         :screen-id     screen-id-value
         :screen-params screen-params}
        [safe-area-context/safe-area-provider
         [:<>
          [inactive]
          [rn/keyboard-avoiding-view
           {:style                    {:flex 1}
            :keyboard-vertical-offset keyboard-vertical-offset}
           (when sheet
             [bottom-sheet/view
              {:insets insets :hide? hide?}
              sheet])]]]]))))

(def toasts (reagent/reactify-component toasts/toasts))

(def alert-banner
  (reagent/reactify-component
   (fn []
     ^{:key (str "alert-banner" @reloader/cnt)}
     [quo.context/provider {:theme :dark}
      [alert-banner/view]])))

(def nfc-sheet-comp
  (reagent/reactify-component
   (fn []
     (let [app-theme (rf/sub [:theme])]
       ^{:key (str "nfc-sheet-" @reloader/cnt)}
       [quo.context/provider {:theme app-theme}
        [rn/keyboard-avoiding-view
         {:style {:position :relative :flex 1}}
         [keycard.sheet/android-view]]]))))

;; LEGACY (should be removed in status 2.0)

(def popover-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "popover" @reloader/cnt)}
     [:<>
      [inactive]
      [popover/popover]
      (when js/goog.DEBUG
        [reloader/reload-view])])))
