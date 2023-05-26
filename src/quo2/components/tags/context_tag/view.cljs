(ns quo2.components.tags.context-tag.view
  (:require [quo2.components.avatars.user-avatar.view :as user-avatar]
            [quo2.components.avatars.group-avatar :as group-avatar]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.tags.context-tag.style :as style]
            [quo2.components.avatars.user-avatar.style :as user-avatar-style]
            [react-native.core :as rn]))

(defn trim-public-key
  [pk]
  (str (subs pk 0 6) "..." (subs pk (- (count pk) 3))))

(defn base-tag
  [{:keys [override-theme style blur?]} & children]
  (into
   [rn/view {:style (merge (style/base-tag override-theme blur?) style)}]
   children))

(defn group-avatar-tag
  [label opts]
  [base-tag
   (-> opts
       (select-keys [:override-theme :style :blur?])
       (assoc-in [:style :padding-left] 3)
       (assoc-in [:style :padding-vertical] 2))
   [group-avatar/group-avatar opts]
   [text/text
    {:weight :medium
     :size   :paragraph-2
     :style  (:text-style opts)}
    (str " " label)]])

(defn public-key-tag
  [params public-key]
  [base-tag params
   [text/text
    {:weight :monospace
     :size   :paragraph-2}
    (trim-public-key public-key)]])

(defn context-tag
  [{:keys [text-style blur? no-avatar-placeholder?] :as params} photo name channel-name]
  (let [text-params       {:weight :medium
                           :size   :paragraph-2
                           :style  (assoc text-style :justify-content :center)}
        empty-photo?      (nil? photo)
        avatar-size       :xxs
        avatar-outer-size (get-in user-avatar-style/sizes [avatar-size :outer])]
    [rn/view {:flex-direction :row}
     [base-tag (assoc-in params [:style :padding-left] 3)
      (if (and empty-photo? no-avatar-placeholder?)
        [rn/view {:style {:width avatar-outer-size}}]
        [user-avatar/user-avatar
         {:full-name         name
          :profile-picture   photo
          :size              avatar-size
          :status-indicator? false}])
      [rn/view {:style style/context-tag-text-container}
       [text/text text-params (str " " name)]
       (when channel-name
         [:<>
          [icons/icon
           :i/chevron-right
           {:color (style/context-tag-icon-color blur?)
            :size  16}]
          [text/text text-params (str "# " channel-name)]])]]]))

(defn user-avatar-tag
  [params username photo]
  [context-tag params photo username])

(defn audio-tag
  [duration params]
  [base-tag (merge {:style style/audio-tag-container} params)
   [rn/view {:style style/audio-tag-icon-container}
    [icons/icon
     :i/play
     {:color style/audio-tag-icon-color
      :size  12}]]
   [text/text
    {:weight :medium
     :size   :paragraph-2
     :style  {:margin-left 4
              :color       (style/audio-tag-text-color (:override-theme params))}}
    duration]])

(defn community-tag
  [avatar community-name {:keys [override-theme] :as params}]
  [context-tag
   (merge {:style      style/community-tag
           :text-style (style/community-tag-text override-theme)}
          params)
   avatar
   community-name])
