(ns status-im.contexts.preview.status-im.main
  (:refer-clojure :exclude [filter])
  (:require
    [quo.context]
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.common :as common]
    [status-im.contexts.preview.status-im.banners.alert-banner :as alert-banner]
    [status-im.contexts.preview.status-im.common.floating-button-page.view :as
     floating-button-page]
    [status-im.contexts.preview.status-im.style :as style]
    [utils.re-frame :as rf]))

(def screens-categories
  {:common [{:name      :screen/floating-button-page
             :component floating-button-page/view}]
   :banner
   [{:name      :screen/alert-banner-preview
     :component alert-banner/view}]})

(defn- category-view
  []
  (let [open?    (reagent/atom false)
        on-press #(swap! open? not)]
    (fn [category]
      [rn/view {:style {:margin-vertical 8}}
       [quo/dropdown
        {:type     :grey
         :state    (if @open? :active :default)
         :on-press on-press}
        (name (key category))]
       (when @open?
         (for [{category-name :name} (val category)]
           ^{:key category-name}
           [quo/button
            {:type            :outline
             :container-style {:margin-vertical 8}
             :on-press        #(rf/dispatch [:navigate-to category-name])}
            (name category-name)]))])))

(defn- main-screen
  []
  (let [theme (quo.context/use-theme)]
    [:<>
     [common/navigation-bar {:title "Status IM components"}]
     [rn/scroll-view {:style (style/main theme)}
      (for [category (sort screens-categories)]
        ^{:key (first category)}
        [category-view category])]]))

(def screens
  (->> screens-categories
       (map val)
       flatten
       (map (fn [subcategory]
              (update-in subcategory
                         [:options :topBar]
                         merge
                         {:visible false})))))

(def main-screens
  [{:name      :screen/status-im-preview
    :options   {:topBar {:visible false}
                :insets {:top? true}}
    :component main-screen}])
