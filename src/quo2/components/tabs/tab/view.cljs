(ns quo2.components.tabs.tab.view
  (:require [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.common.notification-dot.view :as notification-dot]
            [quo2.components.tabs.tab.style :as style]
            [quo2.theme :as quo.theme]
            [react-native.core :as rn]
            [react-native.svg :as svg]))

(defn- right-side-with-cutout
  "SVG exported from Figma."
  [{:keys [height width background-color disabled]}]
  ;; Do not add a view-box property, it'll cause an artifact where the SVG is
  ;; rendered slightly smaller than the proper width and height.
  [svg/svg
   {:width        width
    :height       height
    :fill         background-color
    :fill-opacity (when disabled style/tab-background-opacity)}
   [svg/path
    {:d
     "M 11.468 6.781 C 11.004 6.923 10.511 7 10 7 C 7.239 7 5 4.761 5 2 C 5
     1.489 5.077 0.996 5.219 0.532 C 4.687 0.351 4.134 0.213 3.564 0.123 C 2.787
     0 1.858 0 0 0 L 0 32 C 1.858 32 2.787 32 3.564 31.877 C 7.843 31.199 11.199
     27.843 11.877 23.564 C 12 22.787 12 21.858 12 20 L 12 12 C 12 10.142 12
     9.213 11.877 8.436 C 11.787 7.866 11.649 7.313 11.468 6.781 Z"
     :clip-path "url(#clip0_5514_84289)"}]
   [svg/defs
    [svg/clippath {:id "clip0_5514_84289"}
     [svg/rect {:width width :height height :fill :none}]]]])

(defn- content
  [{:keys [size label]} children]
  [rn/view
   (cond
     (string? children)
     [text/text
      (merge {:size            (case size
                                 24 :paragraph-2
                                 20 :label
                                 nil)
              :weight          :medium
              :number-of-lines 1}
             label)
      children]

     (vector? children)
     children)])

(defn- view-internal
  [{:keys [accessibility-label
           active
           before
           item-container-style
           active-item-container-style
           blur?
           disabled
           id
           on-press
           theme
           segmented?
           size
           notification-dot?
           customization-color]
    :or   {size 32}}
   children]
  (let [show-notification-dot? (and notification-dot? (= size 32))
        {:keys [icon-color
                background-color
                label]}        (style/by-theme {:theme    theme
                                                :blur?    blur?
                                                :disabled disabled
                                                :active   active})]
    [rn/touchable-without-feedback
     (merge {:disabled            disabled
             :accessibility-label accessibility-label}
            (when on-press
              {:on-press (fn []
                           (on-press id))}))
     [rn/view {:style style/container}
      (when show-notification-dot?
        [notification-dot/view
         {:style               style/notification-dot
          :customization-color customization-color}])
      [rn/view
       {:style (merge
                (style/tab
                 {:size                   size
                  :background-color       (if (and segmented? (not active))
                                            :transparent
                                            background-color)
                  :disabled               disabled
                  :segmented?             segmented?
                  :show-notification-dot? show-notification-dot?})
                (if active active-item-container-style item-container-style))}
       (when before
         [rn/view
          [icons/icon before {:color icon-color}]])
       [content {:size size :label label} children]]
      (when show-notification-dot?
        [right-side-with-cutout
         {:width            (style/size->padding-left size)
          :height           size
          :disabled         disabled
          :background-color background-color}])]]))

(def view (quo.theme/with-theme view-internal))
