package cmcc.mobile.admin.activiti.cmd;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ByteArrayEntity;

public class getByteArrayVariableCmd implements Command<ByteArrayEntity> {

	private String byteArrayId;

	public getByteArrayVariableCmd(String byteArrayId) {
		this.byteArrayId = byteArrayId;
	}

	@Override
	public ByteArrayEntity execute(CommandContext commandContext) {
		ByteArrayEntity byteArrayEntity = commandContext.getByteArrayEntityManager().findById(byteArrayId);
		return byteArrayEntity;
	}

}
