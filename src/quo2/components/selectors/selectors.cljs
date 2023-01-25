(ns quo2.components.selectors.selectors
  (:require [quo2.components.icon :as icons]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [quo2.components.selectors.styles :as style]))

(defn- handle-press
  [disabled? on-change checked?]
  (when (not disabled?)
    (fn []
      (swap! checked? not)
      (when on-change (on-change @checked?)))))

(defn checkbox-prefill
  [{:keys [default-checked?]}]
  (let [internal-checked? (reagent/atom (or default-checked? false))]
    (fn [{:keys [on-change disabled? blurred-background? container-style checked?]}]
      (when (and (not (nil? checked?)) (not= @internal-checked? checked?))
        (reset! internal-checked? checked?))
      (let [checked? (or checked? @internal-checked?)]
        [rn/touchable-without-feedback
         {:on-press (handle-press disabled? on-change internal-checked?)}
         [rn/view
          {:style               (merge
                                 container-style
                                 (style/checkbox-prefill blurred-background? disabled?))
           :accessibility-label (str "checkbox-" (if checked? "on" "off"))
           :accessibility-role  :checkbox
           :testID              "checkbox-prefill-component"}
          (when checked?
            [rn/view
             {:style
              {:height 20
               :width  20}}
             [icons/icon :i/check-small
              {:size  20
               :color (colors/theme-colors
                       (colors/alpha colors/neutral-100 (if disabled? 0.3 1))
                       (colors/alpha colors/white (if disabled? 0.3 1)))}]])]]))))

(defn checkbox
  [{:keys [default-checked?]}]
  (let [internal-checked? (reagent/atom (or default-checked? false))]
    (fn [{:keys [on-change disabled? blurred-background? container-style checked?]}]
      (when (and (not (nil? checked?)) (not= @internal-checked? checked?))
        (reset! internal-checked? checked?))
      (let [checked? (or checked? @internal-checked?)]
        [rn/touchable-without-feedback
         {:on-press (handle-press disabled? on-change internal-checked?)}
         [rn/view
          {:style (merge
                   container-style
                   {:height 20
                    :width  20})}
          [rn/view
           {:style               (style/checkbox blurred-background? disabled? checked?)
            :accessibility-label (str "checkbox-" (if checked? "on" "off"))
            :accessibility-role  :checkbox
            :testID              "checkbox-component"}
           (when checked?
             [rn/view
              {:style
               {:height 20
                :width  20}}
              [icons/icon :i/check-small
               {:size  20
                :color colors/white}]])]]]))))

;; TODO (Omar): issue https://github.com/status-im/status-mobile/issues/14681
;(defn checkbox
;  [{:keys [default-checked?]}]
;  (let [checked? (reagent/atom (or default-checked? false))]
;    @(reagent/track
;      (fn [{:keys [on-change disabled? blurred-background? container-style]}]
;        [rn/touchable-without-feedback
;         {:on-press (handle-press disabled? on-change checked?)}
;         [rn/view
;          {:style (merge
;                   container-style
;                   {:height 20
;                    :width  20})}
;          [rn/view
;           {:style               (style/checkbox-toggle checked? disabled? blurred-background?)
;            :accessibility-label (str "checkbox-" (if @checked? "on" "off"))
;            :accessibility-role  :checkbox
;            :testID              "checkbox-component"}
;           (when @checked?
;             [rn/view
;              {:style
;               {:height 20
;                :width  20}}
;              [icons/icon :i/check-small
;               {:size  20
;                :color colors/white}]])]]])
;      checked?)))

(defn radio
  [{:keys [default-checked?]}]
  (let [internal-checked? (reagent/atom (or default-checked? false))]
    (fn [{:keys [on-change disabled? blurred-background? container-style checked?]}]
      (when (and (not (nil? checked?)) (not= @internal-checked? checked?))
        (reset! internal-checked? checked?))
      (let [checked? (or checked? @internal-checked?)]
        [rn/touchable-without-feedback
         {:on-press (handle-press disabled? on-change internal-checked?)}
         [rn/view
          {:style               (merge
                                 container-style
                                 (style/radio checked? disabled? blurred-background?))
           :accessibility-label (str "radio-" (if checked? "on" "off"))
           :accessibility-role  :checkbox
           :testID              "radio-component"}

          [rn/view
           {:style
            (style/radio-inner checked? disabled? blurred-background?)}]]]))))

(defn toggle
  [{:keys [default-checked?]}]
  (let [internal-checked? (reagent/atom (or default-checked? false))]
    (fn [{:keys [on-change disabled? blurred-background? container-style checked?]}]
      (when (and (not (nil? checked?)) (not= @internal-checked? checked?))
        (reset! internal-checked? checked?))
      (let [checked? (or checked? @internal-checked?)]
        [rn/touchable-without-feedback
         {:on-press (handle-press disabled? on-change internal-checked?)}
         [rn/view
          {:style               (merge
                                 container-style
                                 (style/toggle checked? disabled? blurred-background?))
           :accessibility-label (str "toggle-" (if checked? "on" "off"))
           :accessibility-role  :checkbox
           :testID              "toggle-component"}
          [rn/view
           {:style
            (style/toggle-inner checked? disabled? blurred-background?)}]]]))))
