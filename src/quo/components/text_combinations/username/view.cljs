(ns quo.components.text-combinations.username.view
  (:require [quo.components.icon :as icon]
            [quo.components.markdown.text :as text]
            [quo.components.text-combinations.username.style :as style]
            [quo.context]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn- username-text
  [{:keys     [name-type username accessibility-label blur?]
    real-name :name}]
  (let [theme (quo.context/use-theme)]
    [rn/view {:style style/username-text-container}
     [text/text
      {:size                :heading-1
       :accessibility-label accessibility-label
       :weight              :semi-bold}
      username]
     (when (= name-type :nickname)
       [:<>
        [text/text
         {:style  (style/real-name-dot theme blur?)
          :size   :paragraph-1
          :weight :medium}
         "∙"]
        [text/text
         {:style               (style/real-name-text theme blur?)
          :size                :paragraph-1
          :accessibility-label :real-name
          :weight              :medium
          :number-of-lines     1}
         real-name]])]))

(defn- icon-20
  [icon-name color]
  (let [theme (quo.context/use-theme)]
    [icon/icon icon-name
     {:accessibility-label :username-status-icon
      :size                20
      :color               (colors/resolve-color color theme)}]))

(defn status-icon
  [{:keys [theme name-type status]
    :or   {name-type :default}}]
  [rn/view {:style (style/status-icon-container name-type status)}
   (case status
     :verified              [icon-20 :i/verified theme :success]
     :contact               [icon-20 :i/contact theme :blue]
     :untrustworthy         [icon-20 :i/untrustworthy theme :danger]
     :blocked               [icon-20 :i/block theme :danger]
     :untrustworthy-contact [:<>
                             [icon-20 :i/untrustworthy theme :danger]
                             [icon-20 :i/contact theme :blue]]
     nil)])

(defn view
  [props]
  [rn/view {:style style/container}
   [username-text props]
   [status-icon props]])
