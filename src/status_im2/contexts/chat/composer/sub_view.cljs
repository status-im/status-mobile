(ns status-im2.contexts.chat.composer.sub-view
  (:require
    [quo2.core :as quo]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [status-im2.config :as config]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.composer.style :as style]
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
  [{:keys [focused?]}]
  (let [customization-color (rf/sub [:profile/customization-color])
        hide-shell?         (or @focused? @messages.list/show-floating-scroll-down-button)
        y-shell             (reanimated/use-shared-value (if hide-shell? 35 0))
        opacity             (reanimated/use-shared-value (if hide-shell? 0 1))]
    (rn/use-effect
     (fn []
       (reanimated/animate opacity (if hide-shell? 0 1))
       (reanimated/animate y-shell (if hide-shell? 35 0)))
     [@focused? @messages.list/show-floating-scroll-down-button])
    [:<>
     [reanimated/view
      {:style (style/shell-button y-shell opacity)}
      [quo/floating-shell-button
       {:jump-to
        {:on-press            (fn []
                                (when config/shell-navigation-disabled?
                                  (rf/dispatch [:chat/close true]))
                                (rf/dispatch [:shell/navigate-to-jump-to]))
         :customization-color customization-color
         :label               (i18n/label :t/jump-to)
         :style               {:align-self :center}}}
       {}]]
     (when @messages.list/show-floating-scroll-down-button
       [quo/floating-shell-button
        {:scroll-to-bottom {:on-press messages.list/scroll-to-bottom}}
        style/scroll-to-bottom-button])]))

(defn shell-button
  [state animations subscriptions]
  [:f> f-shell-button state animations subscriptions])
