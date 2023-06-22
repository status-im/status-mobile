(ns quo2.components.avatars.user-avatar.view
  (:require [clojure.string :as string]
            [quo2.components.avatars.user-avatar.style :as style]
            [quo2.components.markdown.text :as text]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]))

(defn trim-whitespace [s] (string/join " " (string/split (string/trim s) #"\s+")))

(defn- extract-initials
  [full-name amount-initials]
  (let [upper-case-first-letter (comp string/upper-case first)
        names-list              (string/split (trim-whitespace full-name) " ")]
    (if (= (first names-list) "")
      ""
      (->> names-list
           (map upper-case-first-letter)
           (take amount-initials)
           (string/join)))))

(defn initials-avatar
  [{:keys [full-name size draw-ring? customization-color]}]
  (let [font-size       (get-in style/sizes [size :font-size])
        amount-initials (if (#{:xs :xxs :xxxs} size) 1 2)]
    [rn/view
     {:accessibility-label :initials-avatar
      :style               (style/initials-avatar size draw-ring? customization-color)}
     [text/text
      {:style  style/initials-avatar-text
       :size   font-size
       :weight :semi-bold}
      (extract-initials full-name amount-initials)]]))

(def valid-ring-sizes #{:big :medium :small})

(defn user-avatar
  "If no `profile-picture` is given, draws the initials based on the `full-name` and
  uses `ring-background` to display the ring behind the initials when given. Otherwise,
  shows the `profile-picture` which already comes with the ring drawn."
  [{:keys [full-name status-indicator? online? size profile-picture ring-background
           customization-color static? muted?]
    :or   {status-indicator?   true
           online?             true
           size                :big
           customization-color :turquoise}}]
  (let [full-name    (or full-name "empty name")
        draw-ring?   (when-not muted? (and ring-background (valid-ring-sizes size)))
        outer-styles (style/outer size)
        ;; Once image is loaded, fast image rerenders view with the help of reagent atom,
        ;; But dynamic updates don't work when user-avatar is used inside hole-view
        ;; https://github.com/status-im/status-mobile/issues/15553
        image-view   (if static? rn/image fast-image/fast-image)]
    [rn/view {:style outer-styles :accessibility-label :user-avatar}
     ;; The `profile-picture` already has the ring in it
     (when-let [image (or profile-picture ring-background)]
       [image-view
        {:accessibility-label (if draw-ring? :ring-background :profile-picture)
         :style               outer-styles
         :source              image}])
     (when-not profile-picture
       [initials-avatar
        {:full-name           full-name
         :size                size
         :draw-ring?          draw-ring?
         :customization-color customization-color}])
     (when status-indicator?
       [rn/view
        {:accessibility-label :status-indicator
         :style               (style/dot size draw-ring?)}
        [rn/view
         {:accessibility-label :inner-status-indicator-dot
          :style               (style/inner-dot size online?)}]])]))
