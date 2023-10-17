(ns status-im2.contexts.chat.composer.sub-view
  (:require
    [quo.core :as quo]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [status-im2.config :as config]
    [status-im2.contexts.chat.composer.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn bar
  [theme]
  [rn/view {:style style/bar-container}
   [rn/view {:style (style/bar theme)}]])

(defn f-blur-view
  [{:keys [layout-height focused? theme]}]
  [reanimated/view {:style (style/blur-container layout-height focused?)}
   [blur/view (style/blur-view theme)]])

(defn blur-view
  [props]
  [:f> f-blur-view props])

(defn- f-shell-button
  [{:keys [focused?]} scroll-to-bottom-fn show-floating-scroll-down-button?]
  (let [customization-color (rf/sub [:profile/customization-color])
        hide-shell?         (or @focused? @show-floating-scroll-down-button?)
        y-shell             (reanimated/use-shared-value (if hide-shell? 35 0))
        opacity             (reanimated/use-shared-value (if hide-shell? 0 1))]
    (rn/use-effect
     (fn []
       (reanimated/animate opacity (if hide-shell? 0 1))
       (reanimated/animate y-shell (if hide-shell? 35 0)))
     [@focused? @show-floating-scroll-down-button?])
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
     (when (and (not @focused?)
                @show-floating-scroll-down-button?)
       [quo/floating-shell-button
        {:scroll-to-bottom {:on-press scroll-to-bottom-fn}}
        style/scroll-to-bottom-button])]))

(defn shell-button
  [state scroll-to-bottom-fn show-floating-scroll-down-button?]
  [:f> f-shell-button state scroll-to-bottom-fn show-floating-scroll-down-button?])
