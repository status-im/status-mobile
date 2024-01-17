(ns status-im.contexts.chat.messenger.composer.sub-view
  (:require
    [quo.core :as quo]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.messenger.composer.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.worklets.chat.messenger.composer :as worklets]))

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
  [{:keys [composer-focused?]} chat-list-scroll-y window-height]
  (let [customization-color        (rf/sub [:profile/customization-color])
        scroll-down-button-opacity (worklets/scroll-down-button-opacity
                                    chat-list-scroll-y
                                    composer-focused?
                                    window-height)
        jump-to-button-opacity     (worklets/jump-to-button-opacity
                                    scroll-down-button-opacity
                                    composer-focused?)
        jump-to-button-position    (worklets/jump-to-button-position
                                    scroll-down-button-opacity
                                    composer-focused?)]
    [:<>
     [reanimated/view
      {:style (style/shell-button jump-to-button-position jump-to-button-opacity)}
      [quo/floating-shell-button
       {:jump-to
        {:on-press            #(rf/dispatch [:shell/navigate-to-jump-to])
         :customization-color customization-color
         :label               (i18n/label :t/jump-to)
         :style               {:align-self :center}}}
       {}]]
     [quo/floating-shell-button
      {:scroll-to-bottom {:on-press #(rf/dispatch [:chat.ui/scroll-to-bottom])}}
      style/scroll-to-bottom-button
      scroll-down-button-opacity]]))

(defn shell-button
  [state chat-list-scroll-y window-height]
  [:f> f-shell-button state chat-list-scroll-y window-height])
