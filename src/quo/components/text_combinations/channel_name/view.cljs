(ns quo.components.text-combinations.channel-name.view
  (:require [quo.components.icon :as icon]
            [quo.components.markdown.text :as text]
            [quo.components.text-combinations.channel-name.style :as style]
            [quo.context]
            [react-native.core :as rn]))

(defn icons
  [{:keys [unlocked? muted? blur?]}]
  (let [theme (quo.context/use-theme)]
    [rn/view {:style style/icons-container}
     (when unlocked?
       [rn/view
        {:style               style/icon
         :accessibility-label :channel-name-unlocked-icon}
        [icon/icon :i/unlocked
         {:color (style/unlocked-icon-color theme blur?)
          :size  20}]])

     (when (and unlocked? muted?)
       [rn/view {:style style/icons-gap}])

     (when muted?
       [rn/view
        {:style               style/icon
         :accessibility-label :channel-name-muted-icon}
        [icon/icon :i/muted
         {:color (style/muted-icon-color theme blur?)
          :size  20}]])]))

(defn view
  [{:keys [unlocked? muted? channel-name] :as props}]
  [rn/view {:style style/container}
   [text/text
    {:size   :heading-1
     :weight :semi-bold}
    (str "# " channel-name)]
   (when (or unlocked? muted?)
     [icons props])])
