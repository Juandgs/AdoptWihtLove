    package com.app.adoptwithlove.service;
    import com.app.adoptwithlove.entity.Productos;
    import com.app.adoptwithlove.repository.ProductosRepository;
    import com.app.adoptwithlove.service.Dao.Idao;

    import jakarta.transaction.Transactional;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;

    import java.util.List;

    @Service
    public class ProductoService implements Idao<Productos, Long>{
        @Autowired
        private ProductosRepository producto;

        @Override
        public List<Productos> getAll() {
            return producto.findAll();
        }


        @Override
        public Productos getById(Long id) {
            return producto.findById(id).orElse(null);
        }

        @Transactional
        @Override
        public Productos create(Productos entity){
            return producto.save(entity);
        }


        @Transactional
        @Override
        public Productos update(Long id, Productos entity){
            return producto.save(entity);
        }

        @Transactional
        @Override
        public void delete(Long id) {
            producto.deleteById(id);
        }
    }
