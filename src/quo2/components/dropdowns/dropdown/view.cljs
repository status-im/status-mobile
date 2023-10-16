(ns quo2.components.dropdowns.dropdown.view
  (:require
    [quo2.components.dropdowns.dropdown.properties :as properties]
    [quo2.components.dropdowns.dropdown.style :as style]
    [quo2.components.icon :as icon]
    [quo2.components.markdown.text :as text]
    [quo2.foundations.customization-colors :as customization-colors]
    [quo2.theme :as theme]
    [react-native.blur :as blur]
    [react-native.core :as rn]))

(defn- view-internal
  [{:keys [type size state background customization-color theme on-press icon-name icon? emoji?
           accessibility-label]
    :or   {type      :grey
           size      :size-40
           state     :default
           icon?     false
           emoji?    false
           icon-name :i/placeholder}
    :as   props}
   text]
  (let [{:keys [icon-size text-size emoji-size border-radius]
         :as   size-properties} (properties/sizes size)
        {:keys [left-icon-color right-icon-color right-icon-color-2 label-color blur-type
                blur-overlay-color]
         :as   colors}          (properties/get-colors props)
        right-icon              (if (= state :active) :i/pullup :i/dropdown)
        customization-type?     (= type :customization)
        show-blur-background?   (and (= background :photo)
                                     (= type :grey)
                                     (nil? (properties/sizes-to-exclude-blur-in-photo-bg size)))]
    [rn/pressable
     {:accessibility-label (or accessibility-label :dropdown)
      :disabled            (= state :disabled)
      :on-press            on-press
      :style               (style/root-container size-properties colors props)}
     (when show-blur-background?
       [blur/view
        {:blur-radius   20
         :blur-type     blur-type
         :overlay-color blur-overlay-color
         :style         style/blur-view}])
     (when customization-type?
       [customization-colors/overlay
        {:customization-color customization-color
         :theme               theme
         :pressed?            (= state :active)
         :border-radius       border-radius}])
     (if emoji?
       [rn/text
        {:adjusts-font-size-to-fit true
         :style                    {:font-size emoji-size}}
        text]
       [:<>
        (when icon?
          [icon/icon
           icon-name
           {:accessibility-label :left-icon
            :color               left-icon-color
            :size                icon-size
            :container-style     style/left-icon}])
        [text/text
         {:size            text-size
          :weight          :medium
          :number-of-lines 1
          :style           (style/right-icon label-color)}
         text]
        [icon/icon
         right-icon
         {:size                icon-size
          :accessibility-label :right-icon
          :color               right-icon-color
          :color-2             right-icon-color-2}]])]))

(def view
  "Props:
    - type:       :outline |:grey (default) | :ghost | :customization
    - size:       :size-40 (default) | :size-32 | :size-24
    - state:      :default (default) | :active | :disabled
    - emoji?:     boolean
    - icon?:      boolean
    - background: :blur | :photo (optional)
    - icon-name:  keyword
    - on-press:   function
   
    Child: string - used as label or emoji (for emoji only)"
  (theme/with-theme view-internal))
