(ns quo2.components.drawers.documentation-drawers.view
  (:require [quo2.components.buttons.button.view :as button]
            [quo2.components.drawers.documentation-drawers.style :as style]
            [quo2.components.markdown.text :as text]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [quo2.theme :as quo.theme]))

(defn- view-internal
  "Options
   - `title` Title text
   - `show-button?` Show button
   - `button-label` button label
   - `button-icon` button icon
   - `on-press-button` On press handler for the button
   - `shell?` use shell theme
   `content` Content of the drawer
   "
  [{:keys [title show-button? on-press-button button-label button-icon theme shell?]} content]
  [gesture/scroll-view
   {:style                             style/outer-container
    :always-bounce-vertical            false
    :content-inset-adjustment-behavior :never}
   [rn/view {:style style/container}
    [text/text
     {:size                :heading-2
      :accessibility-label :documentation-drawer-title
      :style               (style/title theme)
      :weight              :semi-bold}
     title]
    [rn/view {:style style/content :accessibility-label :documentation-drawer-content}
     content]
    (when show-button?
      [button/button
       {:size                24
        :type                :outline
        :background          (when shell? :blur)
        :on-press            on-press-button
        :accessibility-label :documentation-drawer-button
        :icon-right          button-icon}
       button-label])]])

(def view (quo.theme/with-theme view-internal))
