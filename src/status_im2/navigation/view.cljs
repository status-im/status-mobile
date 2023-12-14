(ns status-im2.navigation.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as theme]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    schema.view
    [status-im.bottom-sheet.sheets :as bottom-sheets-old]
    [status-im.ui.screens.popover.views :as popover]
    [status-im.ui.screens.profile.visibility-status.views :as visibility-status-views]
    [status-im.ui.screens.signing.views :as signing]
    [status-im2.common.bottom-sheet-screen.view :as bottom-sheet-screen]
    [status-im2.common.bottom-sheet.view :as bottom-sheet]
    [status-im2.common.toasts.view :as toasts]
    [status-im2.constants :as constants]
    [status-im2.navigation.screens :as screens]
    [status-im2.setup.hot-reload :as reloader]
    [utils.i18n :as i18n]
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
  [{:keys [top? bottom?]} background-color]
  (merge
   {:flex             1
    :background-color (or background-color (colors/theme-colors colors/white colors/neutral-100))}
   (when bottom?
     {:padding-bottom (safe-area/get-bottom)})
   (when top?
     {:padding-top (safe-area/get-top)})))


(defn banner-view-internal
  [{:keys [theme show-blur?]}]
  [:<>
   ;; Adding blur for top safe area for screens such as activity center, and scan QR code.
   (when show-blur?
     [blur/view
      {:style        {:position :absolute
                      :top      0
                      :left     0
                      :right    0
                      :height   (safe-area/get-top)}
       :overlayColor colors/neutral-80-opa-80
       :blur-amount  20}])
   [rn/view
    {:style {:position         :absolute
             :top              0
             :left             0
             :right            0
             :z-index          1
             :padding-top      (safe-area/get-top)
             :background-color (colors/resolve-color :warning theme 20)}}
    [quo/text
     {:style  {:padding-vertical 11
               :height           constants/testnet-banner-height
               :color            (colors/resolve-color :warning theme)
               :text-align       :center}
      :weight :medium
      :size   :paragraph-2}
     (i18n/label :t/testnet-mode-enabled)]]])

(def banner-view (quo.theme/with-theme banner-view-internal))

(defn wrapper-view
  [{:keys [component show-blur? hide-testnet-banner?]
    :or   {hide-testnet-banner? false}}]
  (let [testnet-enabled? (rf/sub [:profile/testnet-enabled?])]
    [:<>
     [rn/view
      {:style (cond-> {:flex 1}

                testnet-enabled?
                (assoc :padding-top constants/testnet-banner-height))}
      [component]]
     (when (and testnet-enabled? (not hide-testnet-banner?))
       [banner-view {:show-blur? show-blur?}])]))

(defn screen
  [screen-key]
  (reagent.core/reactify-component
   (fn []
     (let [{:keys [component options]}    (get (if js/goog.DEBUG
                                                 (get-screens)
                                                 screens)
                                               (keyword screen-key))
           {:keys [insets sheet? theme show-blur?
                   hide-testnet-banner?]} options
           user-theme                     (theme/get-theme)
           background-color               (or (get-in options [:layout :backgroundColor])
                                              (when sheet? :transparent))]
       ^{:key (str "root" screen-key @reloader/cnt)}
       [theme/provider {:theme (or theme user-theme)}
        [rn/view {:style (wrapped-screen-style insets background-color)}
         [inactive]
         (if sheet?
           [bottom-sheet-screen/view {:content component}]
           [wrapper-view
            {:component            component
             :show-blur?           show-blur?
             :hide-testnet-banner? hide-testnet-banner?}])]
        (when js/goog.DEBUG
          [:<>
           [reloader/reload-view]
           [schema.view/view]])]))))

(def bottom-sheet
  (reagent/reactify-component
   (fn []
     (let [{:keys [sheets hide?]} (rf/sub [:bottom-sheet])
           sheet                  (last sheets)
           {:keys [theme]}        sheet
           insets                 (safe-area/get-insets)
           user-theme             (theme/get-theme)]
       ^{:key (str "sheet" @reloader/cnt)}
       [theme/provider {:theme (or theme user-theme)}
        [inactive]
        [rn/keyboard-avoiding-view
         {:style                    {:position :relative :flex 1}
          :keyboard-vertical-offset (- (max 20 (:bottom insets)))}
         (when sheet
           [bottom-sheet/view {:insets insets :hide? hide?}
            sheet])]]))))

(def toasts (reagent/reactify-component toasts/toasts))

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

(def visibility-status-popover-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "visibility-status-popover" @reloader/cnt)}
     [rn/view
      [inactive]
      [visibility-status-views/visibility-status-popover]
      (when js/goog.DEBUG
        [reloader/reload-view])])))

(def sheet-comp-old
  (reagent/reactify-component
   (fn []
     ^{:key (str "sheet-old" @reloader/cnt)}
     [:<>
      [inactive]
      [bottom-sheets-old/bottom-sheet]])))

(def signing-comp
  (reagent/reactify-component
   (fn []
     ^{:key (str "signing-sheet" @reloader/cnt)}
     [:<>
      [inactive]
      [signing/signing]
      (when js/goog.DEBUG
        [reloader/reload-view])])))
