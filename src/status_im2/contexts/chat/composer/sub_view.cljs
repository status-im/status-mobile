(ns status-im2.contexts.chat.composer.sub-view
  (:require
    [quo2.core :as quo]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [status-im2.contexts.chat.composer.style :as style]
    [status-im2.contexts.chat.composer.utils :as utils]
    [status-im2.contexts.chat.messages.list.view :as messages.list]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn bar
  []
  [rn/view {:style style/bar-container}
   [rn/view {:style (style/bar)}]])

(defn f-blur-view
  [layout-height focused?]
  [reanimated/view {:style (style/blur-container layout-height focused?)}
   [blur/webview-blur (style/blur-view)]])

(defn blur-view
  [layout-height focused?]
  [:f> f-blur-view layout-height focused?])

(defn- f-shell-button
  [{:keys [maximized?]} {:keys [height]} {:keys [images link-previews? reply edit]}]
  (let [insets       (safe-area/get-insets)
        extra-height (utils/calc-extra-content-height images link-previews? reply edit)
        translate-y  (reanimated/use-shared-value
                      (utils/calc-shell-neg-y insets maximized? extra-height))]
    (rn/use-effect
     (fn []
       (let [extra-height (utils/calc-extra-content-height images link-previews? reply edit)]
         (reanimated/animate translate-y
                             (utils/calc-shell-neg-y insets maximized? extra-height))))
     [@maximized? images link-previews? reply edit])
    [reanimated/view
     {:style (reanimated/apply-animations-to-style
              {:bottom    height ; we use height of the input directly as bottom position
               :transform [{:translate-y translate-y}]} ; translate down when maximized
              {:position :absolute
               :left     0
               :right    0})}
     [quo/floating-shell-button
      (merge {:jump-to
              {:on-press (fn []
                           (rf/dispatch [:chat/close true])
                           (rf/dispatch [:shell/navigate-to-jump-to]))
               :label    (i18n/label :t/jump-to)
               :style    {:align-self :center}}}
             (when @messages.list/show-floating-scroll-down-button
               {:scroll-to-bottom {:on-press messages.list/scroll-to-bottom}}))
      {}]]))

(defn shell-button
  [state animations subs]
  [:f> f-shell-button state animations subs])
