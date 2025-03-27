(ns quo.components.dropdowns.dropdown-input.view
  (:require
    [quo.components.dropdowns.dropdown-input.properties :as properties]
    [quo.components.dropdowns.dropdown-input.style :as style]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.context]
    [react-native.core :as rn]))

(defn view
  "Props:
    - state: :default (default) | :active | :disabled
    - label: string
    - header-label: string
    - icon?: boolean
    - label?: boolean
    - blur?: boolean
    - on-press: function"
  [{:keys [state on-press icon? label? blur? accessibility-label header-label]
    :or   {state        :default
           icon?        true
           label?       true
           blur?        false
           header-label "Label"}
    :as   props}
   label]
  (let [theme          (quo.context/use-theme)
        {:keys [left-icon-color right-icon-color right-icon-color-2]
         :as   colors} (properties/get-colors props)
        right-icon     (if (= state :active) :i/pullup :i/dropdown)]
    [rn/view
     {:style style/root-container}
     (when label?
       [text/text
        {:size   :paragraph-2
         :weight :medium
         :align  :left
         :style  (style/header-label theme blur?)} header-label])
     [rn/pressable
      {:accessibility-label (or accessibility-label :dropdown)
       :disabled            (= state :disabled)
       :on-press            on-press
       :style               (style/container colors props)}
      [rn/view
       {:style style/right-half-container}
       (when icon?
         [icon/icon
          :i/placeholder
          {:accessibility-label :left-icon
           :color               left-icon-color
           :size                properties/icon-size
           :container-style     style/left-icon}])
       [text/text
        {:size            :paragraph-1
         :weight          :regular
         :number-of-lines 1
         :style           (style/right-icon theme)}
        label]]
      [icon/icon
       right-icon
       {:size                properties/icon-size
        :accessibility-label :right-icon
        :color               right-icon-color
        :color-2             right-icon-color-2}]]]))
