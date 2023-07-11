(ns quo2.components.wallet.transaction-progress.view
  (:require [quo2.foundations.colors :as colors]
            [quo2.components.icon :as icon]
            [quo2.components.wallet.transaction-progress.style :as style]
            [react-native.core :as rn]))

(defn transaction-progress-header
  "Component responsible for rendering header"
  [title process]
  (let [failed? (some #(= (:status %) "Failed") process)
        header-content (if failed?
                         [rn/touchable-highlight
                          {:on-press            #(println "Retry button pressed")
                           :accessibility-label :refresh-page-button}
                          [icon/icon :main-icons/refresh]]
                         [icon/icon :i/chevron-right
                          {:size 20
                           :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}])]
    [rn/view {:style style/card-header-styles}
     [rn/view {:style style/card-header-left-styles}
      [icon/icon :i/send
       {:size  20
        :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]]
     [rn/view {:style style/card-header-center-styles}
      [rn/text
       {:style style/card-header-title-styles} title]]
     [rn/view {:style style/card-header-right-styles}
      header-content]]))

(defn- get-icon [status]
  (cond
    (= status "Pending") [icon/icon :i/pending
                          {:size  20
                           :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}] 
    (= status "Failed") [icon/icon :i/negative-state
                         {:size  20
                          :color (colors/theme-colors colors/danger-50 colors/danger-60)}]
    (= status "Confirmed") [icon/icon :i/positive-state
                            {:size  20
                             :color (colors/theme-colors colors/success-50 colors/success-60)}]
    :else [icon/icon :i/positive-state
           {:size  20
            :color (colors/theme-colors (get-in colors/customization [:blue 50]) (get-in colors/customization [:blue 60]))}]))

(defn transaction-progress-status
  "Component responsible for rendering status"
  [{:keys [status progress network]}]
  [rn/view {:style style/card-status-styles}
   [rn/view {:style style/card-header-left-styles}
    (get-icon status)]
   [rn/view {:style style/card-header-center-styles}
    [rn/text {:style style/card-status-text-styles} status " on " network]]
   [rn/view {:style style/card-header-right-styles}
    [rn/text {:style style/card-progress-text-styles} 
     progress]]])

(defn- get-circle-color [status progress index]
  (let [success-colors (colors/theme-colors colors/success-50 colors/success-60)
        neutral-colors (colors/theme-colors colors/neutral-10 colors/neutral-80)
        danger-colors (colors/theme-colors colors/danger-50 colors/danger-60)
        blue-colors (colors/theme-colors (get-in colors/customization [:blue 50]) (get-in colors/customization [:blue 60]))]
    (cond
      (= status "Pending") (cond
                             (or (and (= progress "1/4") (= index 0))
                                 (and (= progress "2/4") (or (= index 0) (= index 3)))
                                 (and (= progress "3/4") (or (= index 0) (= index 3) (= index 6)))
                                 (and (= progress "4/4") (or (= index 0) (= index 3) (= index 6) (= index 9))))
                             success-colors
                             :else
                             neutral-colors)
      (= status "Finalised") (cond
                               (or (= index 0) (= index 3) (= index 6) (= index 9))
                               success-colors
                               :else
                               blue-colors)
      (= status "Confirmed") (cond
                               (or (= index 0) (= index 3) (= index 6) (= index 9))
                               success-colors
                               :else
                               neutral-colors)
      (= status "Failed") (cond
                            (= index 0) danger-colors
                            :else
                            neutral-colors))))

(defn- get-horizontal-bar-color [status component]
  (let [success-colors (colors/theme-colors colors/success-50 colors/success-60)
        neutral-colors (colors/theme-colors colors/neutral-10 colors/neutral-80)
        danger-colors (colors/theme-colors colors/danger-50 colors/danger-60)
        blue-colors (colors/theme-colors (get-in colors/customization [:blue 50]) (get-in colors/customization [:blue 60]))]
    (cond
      (and (= component "left") (= status "Failed")) danger-colors
      (and (= component "left") (= status "Confirmed")) success-colors
      (and (= component "left") (= status "Finalised")) success-colors
      (and (= component "right") (= status "Failed")) neutral-colors
      (and (= component "right") (= status "Confirmed")) neutral-colors
      (and (= component "right") (= status "Finalised")) blue-colors
      :else neutral-colors)))

(defn progress-bar-circle-item
  "Returns a circle"
  [status progress circle]
  [rn/view {:style style/progress-bar-styles}
   [rn/view {:style (merge style/progress-bar-child-styles
                           {:background-color (get-circle-color status progress circle)})}]])

(defn progress-bar-circle
  "Returns the progress bar circle adding their styles"
  [{:keys [status progress]}]
  (let [total-circles 77
        rows 3
        circles-per-row (quot total-circles rows)
        remaining-circles (mod total-circles rows)]
    [rn/view
     {:style {:flex-direction "column" :padding 10}}
     (for [row (range rows)]
       [rn/view
        {:style {:flex-direction "row"
                 :margin-vertical 10}}
        (let [circles (if (< row remaining-circles)
                        (inc circles-per-row)
                        circles-per-row)]
          (for [i (range circles)]
            [progress-bar-circle-item status progress (+ (* rows i) row)]))])]))

(defn progress-bar-horizontal
  "Returns the progress bar horizontal adding their styles" 
  [{:keys [status]}]
  [rn/view {:style style/progress-bar-circle-horizontal-styles}
   [rn/view {:style (merge style/progress-bar-circle-horizontal-child-left-styles
                           {:background-color (get-horizontal-bar-color status "left")})}]
   [rn/view {:style (merge style/progress-bar-circle-horizontal-child-right-styles 
                           {:background-color (get-horizontal-bar-color status "right")})}]
   ])

(defn render_body_horizontal
  [props]
  [rn/view 
   [transaction-progress-status props] 
   [progress-bar-horizontal props]])

(defn render_body_circle
  [props]
  [rn/view
   [transaction-progress-status props]
   [progress-bar-circle props]])

(defn transaction_progress_card
  "Shows the transaction progress card component"
  [{:keys [title process]}]
  [rn/view {:style style/card-styles}
   [transaction-progress-header title process]
   (let [items-count (count process)]
   [rn/flat-list
    {:flex                         1
     :data                         process
     :key-fn                       str
     :render-fn                    (if (> items-count 1)
                                         render_body_horizontal
                                         render_body_circle)}])])
