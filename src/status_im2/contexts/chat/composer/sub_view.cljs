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
   [blur/view (style/blur-view)]])

(defn blur-view
  [layout-height focused?]
  [:f> f-blur-view layout-height focused?])

(defn- f-shell-button
  [{:keys [maximized? focused?]} {:keys [height]}]
  (let [insets         (safe-area/get-insets)
        translate-y    (utils/calc-shell-neg-y insets maximized?)
        translate-hide (reanimated/use-shared-value
                        (if (or @focused? @messages.list/show-floating-scroll-down-button) 30 0))
        opacity        (reanimated/use-shared-value
                        (if (or @focused? @messages.list/show-floating-scroll-down-button) 0 1))]
    (rn/use-effect
     (fn []
       (reanimated/animate opacity
                           (if (or @focused? @messages.list/show-floating-scroll-down-button) 0 1))
       (reanimated/animate translate-hide
                           (if (or @focused? @messages.list/show-floating-scroll-down-button) 30 0)))
     [@focused? @messages.list/show-floating-scroll-down-button])
    [reanimated/view
     {:style (reanimated/apply-animations-to-style
              {:bottom height} ; we use height of the input directly as bottom position
              {:position  :absolute
               :transform [{:translate-y translate-y}]
               :left      0
               :right     0})}
     [reanimated/view
      {:style (reanimated/apply-animations-to-style
               {:transform [{:translate-y translate-hide}]
                :opacity   opacity}
               {})}
      [quo/floating-shell-button
       {:jump-to
        {:on-press (fn []
                     (rf/dispatch [:chat/close true])
                     (rf/dispatch [:shell/navigate-to-jump-to]))
         :label    (i18n/label :t/jump-to)
         :style    {:align-self :center}}}
       {}]]
     [quo/floating-shell-button
      (when @messages.list/show-floating-scroll-down-button
        {:scroll-to-bottom {:on-press messages.list/scroll-to-bottom}})
      {:bottom 24}]]))

(defn shell-button
  [state animations]
  [:f> f-shell-button state animations])
