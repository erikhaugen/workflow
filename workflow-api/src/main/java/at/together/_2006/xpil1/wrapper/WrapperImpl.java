/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.together._2006.xpil1.wrapper;

/**
 *
 * @author slavb
 */
public class WrapperImpl implements Wrapper{

    @Override
    public <T> T unwrap(Class<T> type) throws WrapperNotCapableException {
        if (!isWrapperFor(type))
            throw new WrapperNotCapableException();

        return type.cast(this);
    }

    @Override
    public boolean isWrapperFor(Class<?> type) throws WrapperNotCapableException {
        return type != null && type.isAssignableFrom(getClass());
    }

}
