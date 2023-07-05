(ns quo2.components.drawers.documentation-drawers.view
  (:require [quo2.components.buttons.button :as button]
            [quo2.components.drawers.documentation-drawers.style :as style]
            [quo2.components.markdown.text :as text]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]))

(defn view
  "Options
   - `title` Title text
   - `show-button?` Show button
   - `button-label` button label
   - `button-icon` button icon
   - `on-press-button` On press handler for the button
   - `shell?` use shell theme
   `content` Content of the drawer
   "
  [{:keys [title show-button? on-press-button button-label button-icon shell?]} content]
  [gesture/scroll-view
   {:style                             style/outer-container
    :always-bounce-vertical            false
    :content-inset-adjustment-behavior :never}
   [rn/view {:style style/container}
    [text/text
     {:size                :heading-2
      :accessibility-label :documentation-drawer-title
      :style               (style/title shell?)
      :weight              :semi-bold}
     title]
    [rn/view {:style style/content :accessibility-label :documentation-drawer-content}
     content]
    (when show-button?
      [button/button
       (cond-> {:size                24
                :type                (if shell? :blur-bg-outline :outline)
                :on-press            on-press-button
                :accessibility-label :documentation-drawer-button
                :after               button-icon}
         shell? (assoc :override-theme :dark))
       button-label])]])

