(ns status-im2.contexts.chat.bottom-sheet-composer.sub-view
  (:require
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.bottom-sheet-composer.style :as style]))

(defn bar
  []
  [rn/view {:style style/bar-container}
   [rn/view {:style (style/bar)}]])

(defn blur-view
  [layout-height]
  [:f>
   (fn []
     [reanimated/view {:style (style/blur-container layout-height)}
      [blur/view (style/blur-view)]])])

(defn gradients
  [{:keys [input-ref]}
   {:keys [gradient-z-index text-value focused?]}
   {:keys [gradient-opacity]}
   {:keys [lines]}]
  [:f>
   (fn []
     [:<>
      [reanimated/linear-gradient (style/top-gradient gradient-opacity @gradient-z-index)]
      (when (and (not-empty @text-value) (not @focused?) (> lines 2))
        [rn/touchable-without-feedback
         {:on-press #(when @input-ref (.focus ^js @input-ref))}
         [linear-gradient/linear-gradient (style/bottom-gradient)]])])])
