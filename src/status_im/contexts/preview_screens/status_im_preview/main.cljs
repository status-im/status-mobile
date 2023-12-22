(ns status-im.contexts.preview-screens.status-im-preview.main
  (:refer-clojure :exclude [filter])
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.common :as common]
    [status-im.contexts.preview-screens.status-im-preview.common.floating-button-page.view :as
     floating-button-page]
    [status-im.contexts.preview-screens.status-im-preview.style :as style]
    [utils.re-frame :as rf]))

(def screens-categories
  {:common [{:name      :floating-button-page
             :component floating-button-page/view}]})

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
  [:<>
   [common/navigation-bar {:title "Status IM components"}]
   [rn/scroll-view {:style (style/main)}
    (for [category (sort screens-categories)]
      ^{:key (first category)}
      [category-view category])]])

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
  [{:name      :status-im-preview
    :options   {:topBar {:visible false}
                :insets {:top? true}}
    :component main-screen}])
