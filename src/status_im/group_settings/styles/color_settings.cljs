(ns status-im.group-settings.styles.color-settings)

(def color-highlight
  {:flex 0.25})

(def color-icon-comtainer
  {:justify-content :center
   :align-items     :center})

(defn color-item [color]
  {:width            70
   :height           70
   :border-radius    70
   :background-color color
   :justify-content  :center
   :align-items      :center})

(def icon-ok
  {:width  17.5
   :height 13.5})

(def container-height 225)

(def label-container-top-margin 16)
(def label-container-bottom-margin 8)
(def font-size 16)

(def color-settings-container
  {:position         :absolute
   :right            0
   :left             0
   :bottom           0
   :height           container-height
   :background-color :white
   :border-top-color :#0000001f
   :border-top-width 1
   :align-items      :stretch})

(def label-container
  {:margin-top    label-container-top-margin
   :align-items   :center
   :margin-bottom 8})

(def label
  {:font-size 16
   :color     :black})

(def close-highlight
  {:position :absolute
   :bottom   (- container-height 15
                label-container-top-margin
                label-container-bottom-margin)
   :right    18})

(def close-container-size 24)
(def close-settings-container
  {:width           close-container-size
   :height          close-container-size
   :justify-content :center
   :align-items     :center})

(def close-icon
  {:width  12
   :height 12})

(def all-colors-container
  {:flex-direction :column
   :align-items    :stretch
   :height         (- container-height
                      label-container-top-margin
                      font-size
                      close-container-size)})

(def color-container
  {:flex            0.5
   :flex-direction  :row
   :align-items     :center
   :justify-content :center})
