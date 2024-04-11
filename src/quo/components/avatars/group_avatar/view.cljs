(ns quo.components.avatars.group-avatar.view
  (:require
    [clojure.string :as string]
    [quo.components.avatars.group-avatar.style :as style]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]))

(def sizes
  {:size-20 {:icon      12
             :container 20}
   :size-28 {:icon      16
             :container 28}
   :size-32 {:icon      16
             :container 32}
   :size-48 {:icon      20
             :container 48}
   :size-80 {:icon      32
             :container 80}})

(defn- view-internal
  [_]
  (fn [{:keys [size theme customization-color picture icon-name emoji chat-name]
        :or   {size                :size-20
               customization-color :blue
               picture             nil
               icon-name           :i/members}}]
    (let [container-size (get-in sizes [size :container])
          icon-size      (get-in sizes [size :icon])]
      [rn/view
       {:accessibility-label :group-avatar
        :style               (style/container {:container-size      container-size
                                               :customization-color customization-color
                                               :theme               theme})}
       (if picture
         [fast-image/fast-image
          {:source picture
           :style  {:width  container-size
                    :height container-size}}]
         (cond
           emoji
           [text/text
            {:size  :heading-1
             :style style/emoji-text-style}
            emoji]
           chat-name
           [text/text
            {:size :heading-1}
            ((comp first string/upper-case) chat-name)]
           :else
           [icon/icon icon-name
            {:size  icon-size
             :color colors/white-opa-70}]))])))

(def view (quo.theme/with-theme view-internal))
