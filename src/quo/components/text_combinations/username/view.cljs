(ns quo.components.text-combinations.username.view
  (:require [quo.components.icon :as icon]
            [quo.components.markdown.text :as text]
            [quo.components.text-combinations.username.style :as style]
            [quo.foundations.colors :as colors]
            [quo.theme]
            [react-native.core :as rn]))

(defn- username-text
  [{:keys     [theme name-type username blur?]
    real-name :name}]
  [rn/view {:style style/username-text-container}
   [text/text
    {:size   :heading-1
     :weight :semi-bold}
    username]
   (when (= name-type :nickname)
     [:<>
      [text/text
       {:style  (style/real-name-dot theme blur?)
        :size   :paragraph-1
        :weight :medium}
       "∙"]
      [text/text
       {:style  (style/real-name-text theme blur?)
        :size   :paragraph-1
        :weight :medium}
       real-name]])])

(defn- no-color-icon
  [icon-name theme color]
  [icon/icon icon-name
   {:no-color true
    :size     20
    :color    (colors/resolve-color color theme)}])

(defn status-icon
  [{:keys [theme name-type status]
    :or   {name-type :default}}]
  [rn/view {:style (style/status-icon-container name-type status)}
   (case status
     :verified              [no-color-icon :i/verified theme :success]
     :contact               [no-color-icon :i/contact theme :blue]
     :untrustworthy         [no-color-icon :i/untrustworthy theme :danger]
     :untrustworthy-contact [:<>
                             [no-color-icon :i/untrustworthy theme :danger]
                             [no-color-icon :i/contact theme :blue]]
     :blocked               [icon/icon :i/block
                             {:size  20
                              :color (colors/resolve-color :danger theme)}]
     nil)])

(defn view-internal
  [props]
  [rn/view {:style style/container}
   [username-text props]
   [status-icon props]])

(def view (quo.theme/with-theme view-internal))
