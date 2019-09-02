(ns status-im.ui.components.list-item.styles
  (:require [status-im.ui.components.colors :as colors]))

(defn container [type]
  {:flex-direction     :row
   :justify-content    :flex-start
   :padding-horizontal 16
   :padding-top        (if (= type :section-header) 14 10)
   :padding-bottom     (if (= type :section-header) 4 10)
   :align-items        :center})

(def icon-column-container
  {:margin-right     14
   :padding-vertical 2
   :justify-content  :flex-start
   :max-width        40
   :align-items      :center
   :align-self       :stretch})

(defn icon-container [color]
  {:border-radius    20
   :width            40
   :height           40
   :align-items      :center
   :justify-content  :center
   :background-color (or color colors/blue-light)})

(defn icon [color]
  {:color     (or color colors/blue)
   :font-size 16})

(defn title-column-container [accessories?]
  {:margin-right    (if accessories? 16 0)
   :flex            1
   :justify-content :center
   :align-items     :flex-start
   :align-self      :stretch})

(def title-row-container
  {:min-height      22
   :flex-direction  :row
   :justify-content :flex-start
   :align-self      :stretch})

(defn title-prefix-icon-container
  [title-prefix-height title-prefix-width]
  (merge
   {:width           16
    :height          16
    :justify-content :center
    :margin-top      (if (not title-prefix-height) 4 0)
    :align-items     :center}
   (when title-prefix-width
     {:width title-prefix-width})
   (when title-prefix-height
     {:height title-prefix-height})))

(defn title-prefix-text [type theme icon? subtitle
                         content title-prefix-width disabled?]
  (merge {:margin-left (if icon? 2 0)
          :align-self  :stretch
          ;; we are doing the following to achieve pixel perfection
          ;; as reasonably possible as it can be, and achieve the
          ;; intent of the design spec
          :line-height 22}
         (when (= type :default)
           (if (or subtitle content)
             {:typography  :main-medium
              :line-height 22}
             {:typography  :title
              :line-height 20}))

         (when title-prefix-width
           {:width title-prefix-width})

         (if disabled?
           {:color colors/gray}
           (case theme
             :blue {:color colors/white}
             :action-destructive {:color colors/red}
             :action {:color colors/blue}
             {}))))

(defn title [type theme icon? title-prefix subtitle
             content title-row-accessory disabled?
             title-color-override]
  (merge {:margin-left (if icon? 2 0)
          :flex        1
          :align-self  :stretch
          ;; we are doing the following to achieve pixel perfection
          ;; as reasonably possible as it can be, and achieve the
          ;; intent of the design spec
          :line-height 22}
         (when (= type :default)
           (if (or subtitle content)
             {:typography  :main-medium
              :line-height 22}
             {:typography  :title
              :line-height 20}))

         (when title-prefix
           {:margin-left (if (string? title-prefix) 0 2)})

         (when title-row-accessory
           {:margin-right 16})

         (if (or disabled? (= :section-header type))
           {:color colors/gray}
           ;; else
           (if title-color-override
             {:color title-color-override}
             ;; else
             (case theme
               :blue {:color colors/white}
               :action-destructive {:color colors/red}
               :action {:color colors/blue}
               {})))))

(def title-row-accessory-container
  {:margin-top 2
   :align-self :stretch})

(def subtitle-row-container
  {:min-height      22
   :flex-direction  :row
   :justify-content :flex-start
   :align-self      :stretch})

(defn subtitle [icon? theme subtitle-row-accessory]
  (cond-> {:margin-left (if icon? 2 0)
           :flex        1
           :align-self  :stretch
           :color       colors/gray
           ;; we are doing the following to achieve pixel perfection
           ;; as reasonably possible as it can be, and achieve the
           ;; intent of the design spec
           :line-height 22}
    (= :blue theme)
    (assoc :color (colors/alpha colors/blue-light 0.6))
    subtitle-row-accessory
    (assoc :margin-right 16)))

(def subtitle-row-accessory-container
  {:justify-content :flex-end
   :align-self      :stretch})

(def accessories-container
  {:align-self      :stretch
   :flex-direction  :row
   :align-items     :center
   :justify-content :flex-start})

(defn accessory-text [theme]
  {:color       (if (= theme :blue)
                  (colors/alpha colors/blue-light 0.6)
                  colors/gray)
   ;; we are doing the following to achieve pixel perfection
   ;; as reasonably possible as it can be, and achieve the
   ;; intent of the design spec
   :line-height 22})

(defn radius [size] (/ size 2))

(defn photo [size]
  {:border-radius (radius size)
   :width         size
   :height        size})

(def error
  {:bottom-value 0
   :color        colors/red-light
   :font-size    12})
