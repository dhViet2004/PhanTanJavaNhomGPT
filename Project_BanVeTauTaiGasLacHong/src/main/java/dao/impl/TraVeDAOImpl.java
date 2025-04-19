package dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class TraVeDAOImpl extends UnicastRemoteObject implements dao.TraVeDAO {
    private EntityManager em;

    public TraVeDAOImpl() throws RemoteException {
        this.em = JPAUtil.getEntityManager();
    }

    @Override
    public String getTenTuyenByMaVe(String maVe) {
        try {
            String jpql = """
            SELECT tt.tenTuyen
            FROM VeTau vt
            JOIN vt.lichTrinhTau ltt
            JOIN ltt.tau t
            JOIN t.tuyenTau tt
            WHERE vt.maVe = :maVe
        """;

            return em.createQuery(jpql, String.class)
                    .setParameter("maVe", maVe)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Không tìm thấy
        }
    }

}
