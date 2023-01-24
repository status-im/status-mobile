(ns quo2.components.tabs.tab.view
  (:require [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.tabs.tab.style :as style]
            [quo2.components.notifications.notification-dot :as notification-dot]
            [react-native.core :as rn]
            [react-native.svg :as svg]))

(defn- right-side-with-cutout
  [{:keys [height width background-color]}]
  [svg/svg
   {:width    width
    :height   height
    :view-box (str "0 0 " width " " height)
    :fill     :none}
   [svg/path
    {:fill-rule :evenodd
     :clip-rule :evenodd
     :fill      background-color
     :d
     "M11.4683 6.78094C11.004 6.92336 10.511 7 10 7C7.23858 7 5 4.76142 5 2C5 1.48904 5.07664 0.995988 5.21906 0.531702C4.68658 0.350857 4.13363 0.213283 3.56434 0.123117C2.78702 0 1.85801 0 0 0V32C1.85801 32 2.78702 32 3.56434 31.8769C7.84327 31.1992 11.1992 27.8433 11.8769 23.5643C12 22.787 12 21.858 12 20V12C12 10.142 12 9.21298 11.8769 8.43566C11.7867 7.86637 11.6491 7.31342 11.4683 6.78094Z"}]])

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

(defn view
  [{:keys [accessibility-label
           active
           before
           blur?
           disabled
           id
           on-press
           override-theme
           size
           notification-dot?]
    :or   {size 32}}
   children]
  (let [show-notification-dot? (and notification-dot? (= size 32))
        {:keys [icon-color
                background-color
                label]}
        (style/by-theme {:override-theme override-theme
                         :blur?          blur?
                         :disabled       disabled
                         :active         active})]
    [rn/touchable-without-feedback
     (merge {:disabled            disabled
             :accessibility-label accessibility-label}
            (when on-press
              {:on-press (fn []
                           (on-press id))}))
     [rn/view {:style style/container}
      (when show-notification-dot?
        [rn/view {:style (style/notification-dot notification-dot/size)}
         [notification-dot/notification-dot]])
      [rn/view
       {:style (style/tab {:size                   size
                           :disabled               disabled
                           :background-color       background-color
                           :show-notification-dot? show-notification-dot?})}
       (when before
         [rn/view
          [icons/icon before {:color icon-color}]])
       [content {:size size :label label} children]]
      (when show-notification-dot?
        [right-side-with-cutout
         {:width            (style/size->padding-left size)
          :height           size
          :background-color background-color}])]]))
