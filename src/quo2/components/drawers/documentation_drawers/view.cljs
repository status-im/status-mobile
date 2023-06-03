(ns quo2.components.drawers.documentation-drawers.view
  (:require [quo2.components.markdown.text :as text]
            [quo2.components.drawers.documentation-drawers.style :as style]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [quo2.components.buttons.button :as button]
            [quo2.foundations.colors :as colors]))

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
   [rn/view {:style style/container}
    [text/text
     {:size                :heading-2
      :accessibility-label :documentation-drawer-title
      :style               {:color (colors/theme-colors colors/neutral-100
                                                        colors/white
                                                        (when shell? :dark))}
      :weight              :semi-bold} title]
    [rn/view {:style style/content :accessibility-label :documentation-drawer-content}
     content]
    (when show-button?
      [button/button
       (merge {:size                24
               :type                (if shell? :blur-bg-outline :outline)
               :on-press            on-press-button
               :accessibility-label :documentation-drawer-button
               :after               button-icon}
              (when shell? {:override-theme :dark})) button-label])]])

