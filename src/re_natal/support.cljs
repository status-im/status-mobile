(ns re-natal.support
  (:require [om.next :refer-macros [ui]]))

(defonce root-nodes (atom {}))

(defn root-node!
      "A substitute for a real root node (1) for mounting om-next component.
      You have to call function :on-render and :on-unmount in reconciler :root-render :root-unmount function."
      [id]
      (let [content (atom nil)
            instance (atom nil)
            class (ui Object
                      (componentWillMount [this] (reset! instance this))
                      (render [_] @content))]
           (swap! root-nodes assoc id {:on-render  (fn [el]
                                                       (reset! content el)
                                                       (when @instance
                                                             (.forceUpdate @instance)))
                                       :on-unmount (fn [])
                                       :class      class})
           class))
(defn root-render
      "Use this as reconciler :root-render function."
      [el id]
      (let [node (get @root-nodes id)
            on-render (:on-render node)]
           (when on-render (on-render el))))

(defn root-unmount
      "Use this as reconciler :root-unmount function."
      [id]
      (let [node (get @root-nodes id)
            unmount-fn (:on-unmount node)]
           (when unmount-fn (unmount-fn))))

