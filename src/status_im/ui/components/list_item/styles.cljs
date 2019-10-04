(ns status-im.ui.components.list-item.styles
  (:require [status-im.ui.components.colors :as colors]))

(defn container [type selected?]
  (merge
   {:flex-direction     :row
    :justify-content    :flex-start
    :padding-horizontal 16
    ;; We need `:min-height` to ensure design spec conformance
    ;; and maintain `vertical-rythm` regardless of inner content height.
    ;; Spec: https://www.figma.com/file/cb4p8AxLtTF3q1L6JYDnKN15/Index?node-id=790%3A35

    ;; Without it `:small` type will be height 42 in some cases
    ;; 44 in others. Something similar can happen to `:default`.
    ;; Not really needed for `:section-header` but good to have
    ;; it, if not for anything, for reference.

    ;; - Why not have 15px or 14px top/bottom padding as spec indicates,
    ;;   as a strategy for list-item not collapsing to 42/44 height instead?
    ;; - Why `:small` type has same 10px top/bottom padding like `:default` does
    ;; instead of 14px?

    ;; Because native switch button height(at least in iOS)
    ;; is > 22px(title line-height), and > 24px(accessory icon height in spec).
    ;; Plus there might be a need for <= 32px accessory or something, in edge cases.

    ;; Think of it like an alternate design strategy for components with
    ;; variable content whose height might vary. Like, instead of controlling
    ;; the overall component's height using line-height, top/bottom padding,
    ;; or explicit height.

    ;; And, better to have 32px content vertical space (for :small)
    ;; to play with in this setup; So, we stretch vertically the inner
    ;; containers; and vertically center content inside them. Allows for
    ;; flexibility, with as little constraint and superfluous styling attributes
    ;; as possible.

    ;; Note: this is `min-height` so if we have > 32px accessory or some other
    ;; content inside it `vertical-rythm` can break. We leave it up to the
    ;; list-item consuming implementation to be aware of it.
    :min-height         (case type
                          :default        64
                          :small          52
                          :section-header 40
                          0)
    :padding-top        (if (= type :section-header) 14 10)
    :padding-bottom     (if (= type :section-header) 4 10)
    :align-items        :center}
   (when selected?
     {:background-color colors/blue-light})))

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
             :action-destructive {:color colors/red}
             :action             {:color colors/blue}
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

(defn subtitle [icon? subtitle-row-accessory]
  (cond-> {:margin-left (if icon? 2 0)
           :flex        1
           :align-self  :stretch
           :color       colors/gray
           ;; we are doing the following to achieve pixel perfection
           ;; as reasonably possible as it can be, and achieve the
           ;; intent of the design spec
           :line-height 22}
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

(defn accessory-text [width last]
  {:color       colors/gray
   :max-width   (if last (* @width 0.62) (* @width 0.55))
   ;; we are doing the following to achieve pixel perfection
   ;; as reasonably possible as it can be, and achieve the
   ;; intent of the design spec
   :line-height 22})

(def error
  {:bottom-value 0
   :color        colors/red-light
   :font-size    12})
