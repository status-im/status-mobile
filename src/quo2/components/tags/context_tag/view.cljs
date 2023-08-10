(ns quo2.components.tags.context-tag.view
  (:require [quo2.components.avatars.group-avatar.view :as group-avatar]
            [quo2.components.avatars.user-avatar.style :as user-avatar.style]
            [quo2.components.avatars.user-avatar.view :as user-avatar]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.tags.context-tag.style :as style]
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
   [group-avatar/view opts]
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
  [{:keys [text-style blur? no-avatar-placeholder? text-container-style ellipsize-text? ring?]
    :as   props}
   photo
   name
   channel-name]
  (let [text-props        {:weight          :medium
                           :size            :paragraph-2
                           :style           (assoc text-style :justify-content :center)
                           :number-of-lines 1
                           :ellipsize-mode  :tail}
        empty-photo?      (nil? photo)
        avatar-size       :xxs
        avatar-outer-size (get-in user-avatar.style/sizes [avatar-size :outer])]
    [base-tag (update-in props [:style :padding-left] #(or % 3))
     (if (and empty-photo? no-avatar-placeholder?)
       [rn/view {:style {:width avatar-outer-size}}]
       [user-avatar/user-avatar
        {:full-name         name
         :profile-picture   photo
         :size              avatar-size
         :ring?             ring?
         :status-indicator? false}])
     [rn/view {:style (or text-container-style style/context-tag-text-container)}
      (if ellipsize-text?
        [rn/view {:style {:flex 1}}
         [text/text text-props name]]
        [text/text text-props (str " " name)])
      (when channel-name
        [:<>
         [icons/icon
          :i/chevron-right
          {:color (style/context-tag-icon-color blur?)
           :size  16}]
         [text/text text-props (str "# " channel-name)]])]]))

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
   (merge {:style                style/community-tag
           :text-style           (style/community-tag-text override-theme)
           :text-container-style style/community-tag-text-container
           :ellipsize-text?      true}
          params)
   avatar
   community-name])
